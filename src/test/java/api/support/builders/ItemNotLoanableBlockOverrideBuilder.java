package api.support.builders;

import static org.folio.circulation.support.utils.DateFormatUtil.formatDateTimeOptional;

import org.joda.time.DateTime;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@With
public class ItemNotLoanableBlockOverrideBuilder extends JsonBuilder implements Builder {
  private final DateTime dueDate;

  @Override
  public JsonObject create() {
    JsonObject blockOverrides = new JsonObject();

    put(blockOverrides, "dueDate", formatDateTimeOptional(dueDate));

    return blockOverrides;
  }
}
