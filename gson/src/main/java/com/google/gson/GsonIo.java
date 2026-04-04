package com.google.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.util.Objects;

/**
 * Internal helper for applying and restoring JSON reader/writer configuration while (de-)serializing.
 *
 * <p>This class is package-private and intended for use by Gson's public API implementations.
 */
final class GsonIo {
  private GsonIo() {}

  static WriterContext beginWrite(
      JsonWriter writer, Strictness gsonStrictness, boolean htmlSafe, boolean serializeNulls) {
    Objects.requireNonNull(writer, "writer");
    Strictness oldStrictness = writer.getStrictness();
    boolean oldHtmlSafe = writer.isHtmlSafe();
    boolean oldSerializeNulls = writer.getSerializeNulls();

    writer.setHtmlSafe(htmlSafe);
    writer.setSerializeNulls(serializeNulls);
    applyGsonStrictnessForWriter(writer, gsonStrictness, oldStrictness);
    return new WriterContext(writer, oldStrictness, oldHtmlSafe, oldSerializeNulls);
  }

  static ReaderContext beginRead(JsonReader reader, Strictness gsonStrictness) {
    Objects.requireNonNull(reader, "reader");
    Strictness oldStrictness = reader.getStrictness();
    applyGsonStrictnessForReader(reader, gsonStrictness, oldStrictness);
    return new ReaderContext(reader, oldStrictness);
  }

  /**
   * Used by {@link JsonParser} which must be lenient unless the caller explicitly requested STRICT.
   */
  static ReaderContext beginLenientParse(JsonReader reader) {
    Objects.requireNonNull(reader, "reader");
    Strictness oldStrictness = reader.getStrictness();
    if (oldStrictness != Strictness.STRICT && oldStrictness != Strictness.LENIENT) {
      // For backward compatibility change to LENIENT if reader has default strictness LEGACY_STRICT
      reader.setStrictness(Strictness.LENIENT);
    }
    return new ReaderContext(reader, oldStrictness);
  }

  private static void applyGsonStrictnessForWriter(
      JsonWriter writer, Strictness gsonStrictness, Strictness oldStrictness) {
    if (gsonStrictness != null) {
      writer.setStrictness(gsonStrictness);
    } else if (oldStrictness == Strictness.LEGACY_STRICT) {
      // For backward compatibility change to LENIENT if writer has default strictness LEGACY_STRICT
      writer.setStrictness(Strictness.LENIENT);
    }
  }

  private static void applyGsonStrictnessForReader(
      JsonReader reader, Strictness gsonStrictness, Strictness oldStrictness) {
    if (gsonStrictness != null) {
      reader.setStrictness(gsonStrictness);
    } else if (oldStrictness == Strictness.LEGACY_STRICT) {
      // For backward compatibility change to LENIENT if reader has default strictness LEGACY_STRICT
      reader.setStrictness(Strictness.LENIENT);
    }
  }

  static final class WriterContext implements AutoCloseable {
    private final JsonWriter writer;
    private final Strictness oldStrictness;
    private final boolean oldHtmlSafe;
    private final boolean oldSerializeNulls;
    private boolean closed;

    private WriterContext(
        JsonWriter writer,
        Strictness oldStrictness,
        boolean oldHtmlSafe,
        boolean oldSerializeNulls) {
      this.writer = writer;
      this.oldStrictness = oldStrictness;
      this.oldHtmlSafe = oldHtmlSafe;
      this.oldSerializeNulls = oldSerializeNulls;
    }

    void ensureOpen() {
      // Intentionally empty; avoids "resource is never referenced" warnings with -Werror.
    }

    @Override
    public void close() {
      if (closed) {
        return;
      }
      closed = true;
      writer.setStrictness(oldStrictness);
      writer.setHtmlSafe(oldHtmlSafe);
      writer.setSerializeNulls(oldSerializeNulls);
    }
  }

  static final class ReaderContext implements AutoCloseable {
    private final JsonReader reader;
    private final Strictness oldStrictness;
    private boolean closed;

    private ReaderContext(JsonReader reader, Strictness oldStrictness) {
      this.reader = reader;
      this.oldStrictness = oldStrictness;
    }

    void ensureOpen() {
      // Intentionally empty; avoids "resource is never referenced" warnings with -Werror.
    }

    @Override
    public void close() {
      if (closed) {
        return;
      }
      closed = true;
      reader.setStrictness(oldStrictness);
    }
  }
}

