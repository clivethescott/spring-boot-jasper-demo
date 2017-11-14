package com.example.jasper.demo;

import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@Controller
@RequestMapping("app")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private DataSource dataSource;

    @GetMapping("showReport")
    public String showReportPage() {
        return "someReportPage";
    }

    @PostMapping("exportReport")
    public void generateReport(HttpServletResponse response) {
        System.out.println("Exporting report");

        try (final Connection connection = dataSource.getConnection()) {

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"recon.pdf\"");

            final File reportFile = ResourceUtils.getFile("classpath:static/reports/sample.jrxml");
            final byte[] bytes = JasperConfig.getPdfReportBytes(new HashMap<>(), reportFile, connection);

            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

        } catch (SQLException | IOException | JRException e) {
            logger.debug("An error occurred ", e);
        }
    }
}
