package com.example.osmdroidxp_test;

import org.osmdroid.util.TileSystem;

public class TileSys extends TileSystem {

    int MAXLATID,MAXLONGID;

    TileSys(int x, int y){
        this.MAXLATID = x;
        this.MAXLONGID = y;
    }

    @Override
    public double getX01FromLongitude(final double pLongitude) {
        return (pLongitude - getMinLongitude()) / (getMaxLongitude() - getMinLongitude());
    }

    @Override
    public double getY01FromLatitude(final double pLatitude) {
        return (pLatitude - getMinLatitude()) / (getMaxLatitude() - getMinLatitude());
    }

    @Override
    public double getLongitudeFromX01(final double pX01) {
        return getMinLongitude() + (getMaxLongitude() - getMinLongitude()) * pX01;
    }

    @Override
    public double getLatitudeFromY01(final double pY01) {
        return getMinLatitude() + (getMaxLatitude() - getMinLatitude()) * pY01;
    }

    @Override
    public double getMinLatitude() {
        return 0;
    }

    @Override
    public double getMaxLatitude() {
        return MAXLATID;
    }

    @Override
    public double getMinLongitude() {
        return 0;
    }

    @Override
    public double getMaxLongitude() {
        return MAXLONGID;
    }
}