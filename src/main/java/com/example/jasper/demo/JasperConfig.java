package com.example.jasper.demo;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.util.Map;
import java.util.Objects;

public class JasperConfig {

    private static final Logger logger = LoggerFactory.getLogger(JasperConfig.class);


    public static byte[] getPdfReportBytes(Map<String, Object> parameters, File reportFile, Connection connection)
            throws JRException, FileNotFoundException {

        Objects.requireNonNull(parameters, "Parameters cannot be null");
        Objects.requireNonNull(connection, "Connection cannot be null");
        Objects.requireNonNull(reportFile, "Report file cannot be null");

        final JasperPrint jasperPrint;

        if (reportFile.getPath().endsWith(".jasper")) {
            // No need to re-compile
            jasperPrint = getJasperPrint(parameters, reportFile, connection);
        } else {
            jasperPrint = getJasperPrintAfterCompile(parameters, reportFile, connection);
        }

        Objects.requireNonNull(jasperPrint, "An error occurred while generating the report");


        byte[] bytes = null;

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ExporterInput exporterInput = new SimpleExporterInput(jasperPrint);
        final OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(outputStream);
        final SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        final JRPdfExporter exporter = new JRPdfExporter();


        exporter.setConfiguration(configuration);
        exporter.setExporterInput(exporterInput);
        exporter.setExporterOutput(exporterOutput);


        try {

            logger.debug("Exporting report");

            exporter.exportReport();

            logger.debug("Reported successfully");

            bytes = outputStream.toByteArray();

            logger.debug("Report bytes retrieved");
            logger.debug("Report has {} bytes", Integer.valueOf(bytes.length));

        } catch (JRException jrException) {

            logger.error("An error occurred ", jrException);

        }


        return bytes;
    }


    private static JasperPrint getJasperPrint(Map<String, Object> parameters, File reportFile, Connection connection)
            throws JRException, FileNotFoundException {

        Objects.requireNonNull(parameters, "Parameters cannot be null");
        Objects.requireNonNull(reportFile, "Report file cannot be empty");
        Objects.requireNonNull(connection, "Report connection cannot be null");

        final FileInputStream fileInputStream = new FileInputStream(reportFile);
        final JasperPrint jasperPrint = JasperFillManager.fillReport(fileInputStream, parameters, connection);

        return jasperPrint;
    }


    private static JasperPrint getJasperPrintAfterCompile(Map<String, Object> parameters, File reportFile, Connection connection) {

        Objects.requireNonNull(parameters, "Parameters cannot be null");
        Objects.requireNonNull(reportFile, "Report file cannot be empty");
        Objects.requireNonNull(connection, "Connection cannot be null");

        try (InputStream resourceAsStream = new FileInputStream(reportFile)) {

            return getJasperPrintFromFileResource(parameters, resourceAsStream, connection);

        } catch (JRException | IOException exception) {

            System.err.println("An error occurred while getting SQL jasper print");
            return null;
        }
    }

    private static JasperPrint getJasperPrintFromFileResource(Map<String, Object> parameters,
                                                              InputStream resourceAsStream,
                                                              Connection connection) throws JRException {
        JasperDesign design = JRXmlLoader.load(resourceAsStream);
        JasperReport report = JasperCompileManager.compileReport(design);
        return JasperFillManager.fillReport(report, parameters, connection);
    }


}
