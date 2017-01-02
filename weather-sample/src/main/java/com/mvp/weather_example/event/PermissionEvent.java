package com.mvp.weather_example.event;

public class PermissionEvent {

    private final int requestCode;
    private final String[] permissions;
    private final int[] grantResults;

    public PermissionEvent(int requestCode, String[] permissions, int[] grantResults) {
        this.requestCode = requestCode;
        this.permissions = permissions;
        this.grantResults = grantResults;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public int[] getGrantResults() {
        return grantResults;
    }
}
