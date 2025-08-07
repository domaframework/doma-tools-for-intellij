package doma.example.domain;

import org.seasar.doma.Column;
import org.seasar.doma.Embeddable;

@Embeddable
public class ClientUser {

  final Integer id;
  final String name;

  @Column(name = "nmb")
  final Integer number;

  final ClientUser childEmbedded;
  final ClientUser childEmbedded2;

  public ClientUser(Integer id, String name, Integer number, ClientUser childEmbedded, ClientUser childEmbedded2) {
    this.id = id;
    this.name = name;
    this.number = number;
    this.childEmbedded = childEmbedded;
    this.childEmbedded2 = childEmbedded2;
  }
}
