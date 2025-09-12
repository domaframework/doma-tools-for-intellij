package doma.example.entity;

import doma.example.domain.GuestUser;

/**
 * Class that extends the Embeddable GuestUser
 */
public class SubGuestUser extends GuestUser {

  public String subAccountNumber;

  public SubGuestUser(Integer id, String name, Integer number) {
    super(id, name, number);
  }

  public String getSubAccountNumber() {
    return subAccountNumber;
  }
}
