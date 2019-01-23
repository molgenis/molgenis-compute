package org.molgenis.compute.model;

import java.util.Objects;

/** Output definition. The value can (and often is) a freemarker template */
public class Output extends Input {
  // value, can be a freemarker template
  private String value;

  public Output(String name) {
    super(name);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Output output = (Output) o;
    return Objects.equals(value, output.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "Output{" + "value='" + value + '\'' + "} " + super.toString();
  }
}
