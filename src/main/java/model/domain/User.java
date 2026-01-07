package model.domain;

public class User {
    private String name;
    private int reliabilityScore;
    private int reviewCount;
    private String userType; 

    public User(String name) {
        this.name = name;
        this.reliabilityScore = 0;
        this.reviewCount = 0;
        this.userType = "Collezionista"; 
    }

    public User(String name, int reliabilityScore, int reviewCount) {
        this.name = name;
        this.reliabilityScore = reliabilityScore;
        this.reviewCount = reviewCount;
        this.userType = "Collezionista"; 
    }

    public String getName() {
        return name;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void addReview(int value){
        this.reliabilityScore = ((this.reliabilityScore * this.reviewCount) + value) / (this.reviewCount + 1);
        this.reviewCount += 1;
    }
}
