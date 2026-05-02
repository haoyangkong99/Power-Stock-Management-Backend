package com.powerstock.service;

import com.powerstock.common.BusinessException;
import com.powerstock.dto.request.TransactionRequest;
import com.powerstock.dto.response.TransactionResponse;
import com.powerstock.model.entity.*;
import com.powerstock.model.enums.TransactionType;
import com.powerstock.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock private ItemRepository itemRepository;
    @Mock private InventoryStockRepository stockRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UnitRepository unitRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private AlertService alertService;
    @InjectMocks private TransactionService transactionService;

    private Item testItem;
    private Location testLocation;
    private Unit testUnit;
    private InventoryStock testStock;

    @BeforeEach
    void setUp() {
        testItem = Item.builder().sku("SKU-001").name("Test Item").build();
        testItem.setId(1L);
        testLocation = Location.builder().name("Warehouse").build();
        testLocation.setId(1L);
        testUnit = Unit.builder().name("Piece").symbol("pc").conversionFactor(BigDecimal.ONE).build();
        testUnit.setId(1L);
        testStock = InventoryStock.builder().item(testItem).location(testLocation).currentQuantity(10).build();
        testStock.setId(1L);
    }

    @Test
    void shouldProcessStockIn() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.IN); request.setItemId(1L); request.setQuantity(5);
        request.setUnitId(1L); request.setLocationToId(1L); request.setUnitPrice(BigDecimal.TEN);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(stockRepository.findByItemIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testStock));
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> {
            StockTransaction tx = inv.getArgument(0); tx.setId(1L); return tx;
        });
        when(stockRepository.save(any(InventoryStock.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);
        assertThat(response.getType()).isEqualTo(TransactionType.IN);
        assertThat(response.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldProcessStockOut() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.OUT); request.setItemId(1L); request.setQuantity(3);
        request.setUnitId(1L); request.setLocationToId(1L); request.setUnitPrice(BigDecimal.TEN);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(stockRepository.findByItemIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testStock));
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> {
            StockTransaction tx = inv.getArgument(0); tx.setId(2L); return tx;
        });
        when(stockRepository.save(any(InventoryStock.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);
        assertThat(response.getType()).isEqualTo(TransactionType.OUT);
        assertThat(testStock.getCurrentQuantity()).isEqualTo(7);
    }

    @Test
    void shouldThrowWhenStockOutExceedsAvailable() {
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.OUT); request.setItemId(1L); request.setQuantity(50);
        request.setUnitId(1L); request.setLocationToId(1L); request.setUnitPrice(BigDecimal.TEN);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(stockRepository.findByItemIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testStock));

        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldProcessStockTransfer() {
        Location fromLocation = Location.builder().name("Warehouse B").build();
        fromLocation.setId(2L);
        InventoryStock fromStock = InventoryStock.builder().item(testItem).location(fromLocation).currentQuantity(20).build();
        fromStock.setId(2L);

        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.TRANSFER); request.setItemId(1L); request.setQuantity(5);
        request.setUnitId(1L); request.setLocationFromId(2L); request.setLocationToId(1L); request.setUnitPrice(BigDecimal.TEN);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(fromLocation));
        when(stockRepository.findByItemIdAndLocationId(1L, 2L)).thenReturn(Optional.of(fromStock));
        when(stockRepository.findByItemIdAndLocationId(1L, 1L)).thenReturn(Optional.of(testStock));
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(inv -> {
            StockTransaction tx = inv.getArgument(0); tx.setId(3L); return tx;
        });
        when(stockRepository.save(any(InventoryStock.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);
        assertThat(response.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(fromStock.getCurrentQuantity()).isEqualTo(15);
        assertThat(testStock.getCurrentQuantity()).isEqualTo(15);
    }
}
