package com.powerstock.model.enums;
public enum Permission {
    ITEM_READ(1L << 0), ITEM_CREATE(1L << 1), ITEM_UPDATE(1L << 2), ITEM_DELETE(1L << 3),
    TRANSACTION_READ(1L << 4), TRANSACTION_CREATE(1L << 5),
    LOCATION_READ(1L << 6), LOCATION_CREATE(1L << 7), LOCATION_UPDATE(1L << 8), LOCATION_DELETE(1L << 9),
    USER_READ(1L << 10), USER_CREATE(1L << 11), USER_UPDATE(1L << 12), USER_DELETE(1L << 13),
    PERMISSION_MANAGE(1L << 14), REPORT_READ(1L << 15),
    UNIT_READ(1L << 16), UNIT_CREATE(1L << 17), UNIT_UPDATE(1L << 18), UNIT_DELETE(1L << 19),
    INVENTORY_VIEW_ALL_LOCATIONS(1L << 20);

    private final long mask;
    Permission(long mask) { this.mask = mask; }
    public long getMask() { return mask; }
    public static boolean hasPermission(long permissionMask, Permission permission) { return (permissionMask & permission.mask) != 0; }
    public static long grant(long currentMask, Permission permission) { return currentMask | permission.mask; }
    public static long revoke(long currentMask, Permission permission) { return currentMask & ~permission.mask; }
}
