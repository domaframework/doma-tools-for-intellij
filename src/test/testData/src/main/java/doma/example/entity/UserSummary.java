package doma.example.entity;

@Entity
public class UserSummary {
  private Long userId;
  private String userName;
  private String email;
  private Integer numberOfProjects;

  public UserSummary() {
  }

  public UserSummary(Long userId, String userName, String email, int numberOfProjects) {
    this.userId = userId;
    this.userName = userName;
    this.email = email;
    this.numberOfProjects = numberOfProjects;
  }

  // Inaccessible instance method
  private String getUserName() {
    return userName;
  }

  @Override
  public String toString() {
    return "UserSummary{" +
        "userId=" + userId +
        ", userName='" + userName + '\'' +
        ", email='" + email + '\'' +
        ", numberOfProjects=" + numberOfProjects +
        '}';
  }
}