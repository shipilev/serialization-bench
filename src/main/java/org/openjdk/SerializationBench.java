package org.openjdk;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.openjdk.jmh.annotations.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgsAppend = {"-XX:+UseG1GC", "-Xmx512m"})
@State(Scope.Benchmark)
public class SerializationBench {

    static final FSTConfiguration FST_CONF = FSTConfiguration.createDefaultConfiguration();

    static {
        try {
            // Pre-register classes to work around https://github.com/RuedigerMoeller/fast-serialization/issues/235
            FST_CONF.registerClass(DateTime.class);
            FST_CONF.registerClass(LocalDate.class);
            FST_CONF.registerClass(Class.forName("org.joda.time.chrono.ISOChronology$Stub"));
            FST_CONF.registerClass(Class.forName("org.joda.time.DateTimeZone$Stub"));
            FST_CONF.registerClass(Class.forName("org.openjdk.TestObject"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Param({"1", "10", "100"})
    int count;

    Object[] objs;

    @Setup
    public void setup() {
        objs = new Object[count];
        for (int c = 0; c < count; c++) {
            objs[c] = new TestObject();
        }
    }

    @Benchmark
    public Object fst() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (FSTObjectOutput fstoo = new FSTObjectOutput(out, FST_CONF)) {
            fstoo.writeObject(objs);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (FSTObjectInput fstin = new FSTObjectInput(in, FST_CONF)) {
            return fstin.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Benchmark
    public Object java() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(objs);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(in)) {
            return ois.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
