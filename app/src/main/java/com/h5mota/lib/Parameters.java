package com.h5mota.lib;

public class Parameters {
  public String name;
  public String value;

  public Parameters(String _name, String _value) {
    name = _name;
    value = _value;
  }

  public static Parameters create(String name, String value) {
    return new Parameters(name, value);
  }
}
