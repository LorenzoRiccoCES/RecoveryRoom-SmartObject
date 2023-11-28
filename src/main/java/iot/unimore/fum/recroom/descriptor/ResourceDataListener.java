package iot.unimore.fum.recroom.descriptor;

public interface ResourceDataListener<T> {
    public void onDataChanged(SmartObjectResource<T> resource, T updatedValue);
}
