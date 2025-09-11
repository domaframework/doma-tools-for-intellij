package doma.example.entity;

import java.time.LocalDate;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;

// Parent class that is not Entity
public class User {

  @Id
  public Integer userId;
  private String userName;
  private String email;

  public User() {
  }

  public User(Long userId, String userName, String email) {
    this.userId = userId;
    this.userName = userName;
    this.email = email;
  }

  // accessible parent public method
  public String getUserNameFormat() {
    return "User:" + userName;
  }

  // Inaccessible parent private method
  private String getEmail(){
    return email;
  }
  
  public String processText(CharSequence text) {
    return text.toString();
  }
  
  public CharSequence getDescription() {
    return "User description";
  }

}