package org.openjdk;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public final class TestObject implements Serializable {
    private static final long serialVersionUID = -9083923722080054771L;

    private String string = "asdasdassd";

    private DateTime dateTime = new DateTime();

    private LocalDate locaTime = new LocalDate();

    private int inty = 129;
}