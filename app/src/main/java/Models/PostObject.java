package Models;

public class PostObject {
    private String docId;
    private String titleVal;
    private String imageVal;
    private String userIdVal;
    private String userPhotoVal;

    private String latitudeVal;
    private String longitudeVal;
    private String locationNameVal;
    private String geoHashVal;

    private String startDateVal;
    private String endDateVal;

    public PostObject(String titleVal, String imageVal, String userIdVal, String userPhotoVal, String latitudeVal, String longitudeVal, String locationNameVal, String geoHashVal, String startDateVal, String endDateVal) {
        this.titleVal = titleVal;
        this.imageVal = imageVal;
        this.userIdVal = userIdVal;
        this.userPhotoVal = userPhotoVal;
        this.latitudeVal = latitudeVal;
        this.longitudeVal = longitudeVal;
        this.locationNameVal = locationNameVal;
        this.geoHashVal = geoHashVal;
        this.startDateVal = startDateVal;
        this.endDateVal = endDateVal;
    }

    public PostObject(){
    }

    public String getDocId() {
        return docId;
    }
    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTitleVal() {
        return titleVal;
    }
    public String getImageVal() {
        return imageVal;
    }
    public String getUserIdVal() {
        return userIdVal;
    }
    public String getUserPhotoVal() {
        return userPhotoVal;
    }
    public String getLatitudeVal() {
        return latitudeVal;
    }
    public String getLongitudeVal() {
        return longitudeVal;
    }
    public String getLocationNameVal() {
        return locationNameVal;
    }
    public String getGeoHashVal() {
        return geoHashVal;
    }
    public String getStartDateVal() {
        return startDateVal;
    }
    public String getEndDateVal() {
        return endDateVal;
    }

    public void setTitleVal(String titleVal) {
        this.titleVal = titleVal;
    }
    public void setImageVal(String imageVal) {
        this.imageVal = imageVal;
    }
    public void setUserIdVal(String userIdVal) {
        this.userIdVal = userIdVal;
    }
    public void setUserPhotoVal(String userPhotoVal) {
        this.userPhotoVal = userPhotoVal;
    }
    public void setLatitudeVal(String latitudeVal) {
        this.latitudeVal = latitudeVal;
    }
    public void setLongitudeVal(String longitudeVal) {
        this.longitudeVal = longitudeVal;
    }
    public void setLocationNameVal(String locationNameVal) {
        this.locationNameVal = locationNameVal;
    }
    public void setGeoHashVal(String geoHashVal) {
        this.geoHashVal = geoHashVal;
    }
    public void setStartDateVal(String startDateVal) {
        this.startDateVal = startDateVal;
    }
    public void setEndDateVal(String endDateVal) {
        this.endDateVal = endDateVal;
    }
}
