package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.TransactionRequest;
import com.powerstock.dto.response.StockResponse;
import com.powerstock.dto.response.TransactionResponse;
import com.powerstock.model.entity.*;
import com.powerstock.model.enums.TransactionType;
import com.powerstock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final InventoryStockRepository stockRepository;
    private final ItemRepository itemRepository;
    private final UnitRepository unitRepository;
    private final LocationRepository locationRepository;
    private final AlertService alertService;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("ITEM_NOT_FOUND", "Item not found"));
        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new BusinessException("UNIT_NOT_FOUND", "Unit not found"));
        Location locationTo = locationRepository.findById(request.getLocationToId())
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Destination location not found"));
        BigDecimal totalValue = unit.getConversionFactor()
                .multiply(BigDecimal.valueOf(request.getQuantity())).multiply(request.getUnitPrice());

        StockTransaction transaction = StockTransaction.builder()
                .type(request.getType()).item(item).quantity(request.getQuantity())
                .unit(unit).unitPrice(request.getUnitPrice()).totalValue(totalValue)
                .locationTo(locationTo).synced(false).build();

        switch (request.getType()) {
            case IN -> processStockIn(request, item, locationTo);
            case OUT -> processStockOut(request, item, locationTo);
            case TRANSFER -> processTransfer(request, item, locationTo, transaction);
        }
        return toResponse(transactionRepository.save(transaction));
    }

    private void processStockIn(TransactionRequest request, Item item, Location location) {
        InventoryStock stock = stockRepository.findByItemIdAndLocationId(item.getId(), location.getId())
                .orElseGet(() -> InventoryStock.builder().item(item).location(location).currentQuantity(0).build());
        stock.setCurrentQuantity(stock.getCurrentQuantity() + request.getQuantity());
        stock.setLastUpdated(Instant.now());
        stockRepository.save(stock);
        alertService.checkAndAlert(item, location, stock.getCurrentQuantity());
    }

    private void processStockOut(TransactionRequest request, Item item, Location location) {
        InventoryStock stock = stockRepository.findByItemIdAndLocationId(item.getId(), location.getId())
                .orElseThrow(() -> new BusinessException("NO_STOCK_RECORD", "No stock record found for this item at this location"));
        if (stock.getCurrentQuantity() < request.getQuantity())
            throw new BusinessException("INSUFFICIENT_STOCK",
                    "Insufficient stock. Available: " + stock.getCurrentQuantity() + ", Requested: " + request.getQuantity());
        stock.setCurrentQuantity(stock.getCurrentQuantity() - request.getQuantity());
        stock.setLastUpdated(Instant.now());
        stockRepository.save(stock);
        alertService.checkAndAlert(item, location, stock.getCurrentQuantity());
    }

    private void processTransfer(TransactionRequest request, Item item, Location locationTo, StockTransaction transaction) {
        if (request.getLocationFromId() == null)
            throw new BusinessException("TRANSFER_SOURCE_REQUIRED", "Source location is required for transfers");
        Location locationFrom = locationRepository.findById(request.getLocationFromId())
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "Source location not found"));
        InventoryStock fromStock = stockRepository.findByItemIdAndLocationId(item.getId(), locationFrom.getId())
                .orElseThrow(() -> new BusinessException("NO_STOCK_RECORD", "No stock record found at source location"));
        if (fromStock.getCurrentQuantity() < request.getQuantity())
            throw new BusinessException("INSUFFICIENT_STOCK",
                    "Insufficient stock at source. Available: " + fromStock.getCurrentQuantity());

        InventoryStock toStock = stockRepository.findByItemIdAndLocationId(item.getId(), locationTo.getId())
                .orElseGet(() -> InventoryStock.builder().item(item).location(locationTo).currentQuantity(0).build());

        fromStock.setCurrentQuantity(fromStock.getCurrentQuantity() - request.getQuantity());
        fromStock.setLastUpdated(Instant.now());
        toStock.setCurrentQuantity(toStock.getCurrentQuantity() + request.getQuantity());
        toStock.setLastUpdated(Instant.now());
        stockRepository.save(fromStock);
        stockRepository.save(toStock);
        alertService.checkAndAlert(item, locationFrom, fromStock.getCurrentQuantity());
        alertService.checkAndAlert(item, locationTo, toStock.getCurrentQuantity());
        transaction.setLocationFrom(locationFrom);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByItem(Long itemId, Pageable pageable) {
        return transactionRepository.findByItemId(itemId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByItem(Long itemId) {
        return stockRepository.findByItemId(itemId).stream().map(this::toStockResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getStockByLocation(Long locationId) {
        return stockRepository.findByLocationId(locationId).stream().map(this::toStockResponse).toList();
    }

    private TransactionResponse toResponse(StockTransaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId()).type(tx.getType()).itemId(tx.getItem().getId())
                .itemSku(tx.getItem().getSku()).itemName(tx.getItem().getName())
                .quantity(tx.getQuantity()).unitSymbol(tx.getUnit().getSymbol())
                .unitPrice(tx.getUnitPrice()).totalValue(tx.getTotalValue())
                .locationFromId(tx.getLocationFrom() != null ? tx.getLocationFrom().getId() : null)
                .locationFromName(tx.getLocationFrom() != null ? tx.getLocationFrom().getName() : null)
                .locationToId(tx.getLocationTo().getId()).locationToName(tx.getLocationTo().getName())
                .synced(tx.getSynced()).createdAt(tx.getCreatedAt()).build();
    }

    private StockResponse toStockResponse(InventoryStock stock) {
        return StockResponse.builder()
                .id(stock.getId()).itemId(stock.getItem().getId())
                .itemSku(stock.getItem().getSku()).itemName(stock.getItem().getName())
                .locationId(stock.getLocation().getId()).locationName(stock.getLocation().getName())
                .currentQuantity(stock.getCurrentQuantity())
                .stockValue(stock.getItem().getBasePrice().multiply(BigDecimal.valueOf(stock.getCurrentQuantity())))
                .lastUpdated(stock.getLastUpdated()).build();
    }
}
