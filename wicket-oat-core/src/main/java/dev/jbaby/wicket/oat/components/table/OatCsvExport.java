package dev.jbaby.wicket.oat.components.table;

import org.apache.wicket.Application;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.IExportableColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Writes a data table's rows as CSV (RFC 4180, UTF-8 with a byte order mark so
 * spreadsheet apps detect the encoding), for {@code OatDataTable.setCsvExport}.
 * <ul>
 * <li>The columns are those with a header text and a value ({@link IExportableColumn}).</li>
 * <li>Values stay machine-readable: numbers as plain digits with a {@code .} decimal
 * point, dates and times in ISO 8601, booleans as {@code true}/{@code false}; anything
 * else through the application's converter for its type.</li>
 * <li>A text starting with {@code = + - @}, a tab or a carriage return is prefixed with
 * {@code '}, so a spreadsheet doesn't run it as a formula (CSV injection).</li>
 * </ul>
 */
public final class OatCsvExport {

    /** Rows are fetched from the provider this many at a time. */
    private static final int BATCH = 500;

    private OatCsvExport() {
    }

    /** Sends the rows as a CSV download, as the response to the current request. */
    public static <T> void download(RequestCycle cycle, IDataProvider<T> provider, List<? extends IColumn<T, ?>> columns,
                                    String fileName, Locale locale) {
        AbstractResourceStreamWriter stream = new AbstractResourceStreamWriter() {
            @Override
            public void write(OutputStream output) throws IOException {
                OatCsvExport.write(provider, columns, locale, output);
            }

            @Override
            public String getContentType() {
                return "text/csv; charset=UTF-8";
            }
        };
        cycle.scheduleRequestHandlerAfterCurrent(new ResourceStreamRequestHandler(stream, fileName)
                .setContentDisposition(ContentDisposition.ATTACHMENT));
    }

    /** Writes the header line and every row. */
    @SuppressWarnings("unchecked")
    public static <T> void write(IDataProvider<T> provider, List<? extends IColumn<T, ?>> columns, Locale locale,
                                 OutputStream output) throws IOException {
        List<IExportableColumn<T, ?>> exported = columns.stream()
                .filter(IExportableColumn.class::isInstance)
                .<IExportableColumn<T, ?>>map(column -> (IExportableColumn<T, ?>) column)
                .filter(column -> column.getDisplayModel() != null && column.getDisplayModel().getObject() != null
                        && !column.getDisplayModel().getObject().isBlank())
                .toList();
        Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        writer.write('﻿');
        writeLine(writer, exported.stream().map(column -> text(column.getDisplayModel().getObject())).toList());

        long size = provider.size();
        for (long first = 0; first < size; first += BATCH) {
            Iterator<? extends T> rows = provider.iterator(first, Math.min(BATCH, size - first));
            while (rows.hasNext()) {
                IModel<T> row = provider.model(rows.next());
                writeLine(writer, exported.stream().map(column -> {
                    IModel<?> value = column.getDataModel(row);
                    try {
                        return value(value == null ? null : value.getObject(), locale);
                    } finally {
                        if (value != null) {
                            value.detach();
                        }
                    }
                }).toList());
                row.detach();
            }
        }
        writer.flush();
        provider.detach();
    }

    private static void writeLine(Writer writer, List<String> cells) {
        try {
            for (int i = 0; i < cells.size(); i++) {
                if (i > 0) {
                    writer.write(',');
                }
                writer.write(quote(cells.get(i)));
            }
            writer.write("\r\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A value as CSV text; numbers and dates stay machine-readable. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static String value(Object value, Locale locale) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof TemporalAccessor) {
            return value.toString();
        }
        if (value instanceof CharSequence || value instanceof Enum<?>) {
            return text(value.toString());
        }
        IConverter converter = Application.get().getConverterLocator().getConverter(value.getClass());
        return text(converter != null ? converter.convertToString(value, locale) : value.toString());
    }

    /** Text, defused if a spreadsheet would read it as a formula. */
    static String text(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        char first = text.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r' ? "'" + text : text;
    }

    /** Quotes a cell when it holds a comma, a quote or a line break. */
    static String quote(String cell) {
        if (cell.indexOf(',') >= 0 || cell.indexOf('"') >= 0 || cell.indexOf('\n') >= 0 || cell.indexOf('\r') >= 0) {
            return '"' + cell.replace("\"", "\"\"") + '"';
        }
        return cell;
    }
}
