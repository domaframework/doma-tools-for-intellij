package doma.example.entity;

import java.util.List;

public class Principal {

  private List<Permission> permissions;

  public static class Permission {
    public String name;
    public String description;
  }
}