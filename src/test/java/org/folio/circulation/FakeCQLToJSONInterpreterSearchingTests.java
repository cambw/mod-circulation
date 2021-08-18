package org.folio.circulation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import api.support.fakes.FakeCQLToJSONInterpreter;
import io.vertx.core.json.JsonObject;

public class FakeCQLToJSONInterpreterSearchingTests {

  @Test
  public void canFilterBySinglePropertyContains() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject().put("myProperty", "food"));
    records.add(new JsonObject().put("myProperty", "party"));

    List<JsonObject> matchedRecords =
      interpreter.execute(records, "myProperty=foo");

    assertThat(matchedRecords.size(), is(1));
  }

  @Test
  public void canFilterBySinglePropertyExactMatch() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject().put("myProperty", "foo"));
    records.add(new JsonObject().put("myProperty", "food"));
    records.add(new JsonObject().put("myProperty", "party"));

    List<JsonObject> matchedRecords =
      interpreter.execute(records, "myProperty==foo");

    assertThat(matchedRecords.size(), is(1));
  }

  @Test
  public void canFilterBySingleOptionalProperty() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject().put("myProperty", "foo"));
    records.add(new JsonObject());

    List<JsonObject> matchedRecords =
      interpreter.execute(records, "myProperty=foo");

    assertThat(matchedRecords.size(), is(1));
  }

  @Test
  public void canFilterByMultipleProperties() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject()
      .put("myProperty", "foo")
      .put("otherProperty", "nope"));

    records.add(new JsonObject()
      .put("myProperty", "bar")
      .put("otherProperty", "match"));

    records.add(new JsonObject()
      .put("myProperty", "foo")
      .put("otherProperty", "contains match"));

    records.add(new JsonObject()
      .put("myProperty", "food")
      .put("otherProperty", "contains match"));

    records.add(new JsonObject()
      .put("myProperty", "foo")
      .put("otherProperty", "match"));

    records.add(new JsonObject()
      .put("myProperty", "food")
      .put("otherProperty", "match"));

    List<JsonObject> matchedRecords =
      interpreter.execute(records, "myProperty=foo and otherProperty==match");

    assertThat(matchedRecords.size(), is(2));
  }

  @Test
  public void canFilterByMultipleValues() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject()
      .put("myProperty", "baz"));

    JsonObject shouldMatch = new JsonObject()
      .put("myProperty", "bar");

    records.add(shouldMatch);

    JsonObject shouldAlsoMatch = new JsonObject()
      .put("myProperty", "foo");

    records.add(shouldAlsoMatch);

    List<JsonObject> matchedRecords =
      interpreter.execute(records, "myProperty=(foo or bar)");

    assertThat(matchedRecords.size(), is(2));
  }

  @Test
  public void canFilterBySingleValueWithBrackets() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject()
      .put("myProperty", "baz"));

    records.add(new JsonObject()
      .put("myProperty", "bar"));

    records.add(new JsonObject()
      .put("myProperty", "foo"));

    List<JsonObject> matchedRecords =
      interpreter.execute(records, "myProperty=(foo)");

    assertThat(matchedRecords.size(), is(1));
  }

  @Test
  public void canFilterByCombinationOfSingleAndMultipleValues() {
    FakeCQLToJSONInterpreter interpreter = new FakeCQLToJSONInterpreter();

    Collection<JsonObject> records = new ArrayList<>();

    records.add(new JsonObject()
      .put("id", "12345")
      .put("otherProperty", "delivery")
      .put("myProperty", "baz"));

    records.add(new JsonObject()
      .put("id", "54659")
      .put("otherProperty", "hold")
      .put("myProperty", "bar"));

    records.add(new JsonObject()
      .put("id", "56049")
      .put("otherProperty", "hold")
      .put("myProperty", "foo"));

    records.add(new JsonObject()
      .put("id", "12345")
      .put("otherProperty", "delivery")
      .put("myProperty", "bar"));

    records.add(new JsonObject()
      .put("id", "12345")
      .put("otherProperty", "delivery")
      .put("myProperty", "foo"));

    records.add(new JsonObject()
      .put("id", "12345")
      .put("otherProperty", "delivery")
      .put("myProperty", "bar"));

    records.add(new JsonObject()
      .put("id", "12345")
      .put("otherProperty", "hold")
      .put("myProperty", "foo"));

    records.add(new JsonObject()
      .put("id", "12345")
      .put("otherProperty", "hold")
      .put("myProperty", "bar"));

    List<JsonObject> matchedRecords =
      interpreter.execute(records,
        "id==12345 and myProperty==(foo or bar) and otherProperty==(hold)");

    assertThat(matchedRecords.size(), is(2));
  }
}
