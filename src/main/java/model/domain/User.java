package model.domain;

public class User {
    private long id; 
    private String name;
    private int reliabilityScore;
    private int reviewCount;
    private String userType;

    public User(String name, int reliabilityScore, int reviewCount) {
        this.name = name;
        this.reliabilityScore = reliabilityScore;
        this.reviewCount = reviewCount;
        this.userType = config.AppConfig.USER_TYPE_COLLECTOR;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getReliabilityScore() {
        return reliabilityScore;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void addReview(int value) {
        this.reliabilityScore = ((this.reliabilityScore * this.reviewCount) + value) / (this.reviewCount + 1);
        this.reviewCount += 1;
    }
}
