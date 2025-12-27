package com.projectblackberry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectblackberry.model.WeeklyChecklist;
import com.projectblackberry.model.DayChecklist;
import com.projectblackberry.model.TaskItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMain {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, WeeklyChecklist> checklists = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        loadAllChecklists();
        
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new HomeHandler());
        server.createContext("/today", new TodayHandler());
        server.createContext("/setup", new SetupHandler());
        server.createContext("/import", new ImportHandler());
        server.createContext("/checklist", new ChecklistHandler());
        server.createContext("/navigator", new NavigatorHandler());

        server.setExecutor(null);
        System.out.println("Project BlackBerry server running on port " + port);
        System.out.println("Endpoints:");
        System.out.println("  GET  /              (Home/Setup)");
        System.out.println("  GET  /today         (Legacy home)");
        System.out.println("  GET  /setup         (Setup guide)");
        System.out.println("  POST /import?week=2025-W1");
        System.out.println("  GET  /checklist?week=2025-W1");
        System.out.println("  GET  /navigator");
        server.start();
    }

    private static void loadAllChecklists() {
        try {
            java.nio.file.Path dataDir = java.nio.file.Paths.get("data/checklists");
            if (java.nio.file.Files.exists(dataDir)) {
                java.nio.file.Files.list(dataDir)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        try {
                            String filename = p.getFileName().toString();
                            String weekId = filename.substring(0, filename.length() - 5);
                            WeeklyChecklist checklist = ChecklistStorage.loadChecklist(weekId);
                            if (checklist != null) {
                                checklists.put(weekId, checklist);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading " + p + ": " + e.getMessage());
                        }
                    });
                System.out.println("Loaded " + checklists.size() + " checklist(s) from disk");
            }
        } catch (Exception e) {
            System.err.println("Error loading checklists: " + e.getMessage());
        }
    }

    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }


            String response = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Project BlackBerry - Home</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
                            .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 5px; }
                            h1 { color: #333; border-bottom: 2px solid #0066cc; padding-bottom: 10px; }
                            .status { padding: 15px; margin: 20px 0; border-radius: 5px; background: #e8f4f8; border-left: 4px solid #0066cc; }
                            .status.success { background: #e8f5e9; border-color: #4caf50; }
                            .nav { margin: 20px 0; }
                            .nav a { display: inline-block; margin: 5px 10px 5px 0; padding: 10px 15px; background: #0066cc; color: white; text-decoration: none; border-radius: 3px; }
                            .nav a:hover { background: #0052a3; }
                            .info { background: #fff3cd; padding: 10px; border-radius: 3px; margin: 10px 0; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Project BlackBerry</h1>
                            <div class="status success">
                                <strong>✓ Connected!</strong> You're successfully connected to the server.
                            </div>
                            <div class="nav">
                                <a href="/checklist">📋 Weekly Checklist</a>
                                <a href="/navigator">🔗 Resource Navigator</a>
                                <a href="/setup">⚙️ Setup Guide</a>
                            </div>
                            <div class="info">
                                <strong>Quick Start:</strong> Bookmark this page on your Z10 for easy access. 
                                Use the Setup Guide if you need to configure your connection.
                            </div>
                        </div>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, 200, response, "text/html; charset=utf-8");
        }
    }

    static class SetupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String serverIP = exchange.getLocalAddress().getAddress().getHostAddress();
            if (serverIP.equals("0.0.0.0") || serverIP.equals("::")) {
                try {
                    serverIP = java.net.InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {
                    serverIP = "YOUR_LAPTOP_IP";
                }
            }

            String response = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Setup Guide - Project BlackBerry</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
                            .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 5px; }
                            h1 { color: #333; border-bottom: 2px solid #0066cc; padding-bottom: 10px; }
                            h2 { color: #666; margin-top: 30px; }
                            .step { margin: 20px 0; padding: 15px; background: #fafafa; border-left: 4px solid #0066cc; }
                            .step-number { display: inline-block; width: 30px; height: 30px; background: #0066cc; color: white; text-align: center; line-height: 30px; border-radius: 50%; margin-right: 10px; font-weight: bold; }
                            .code { background: #f4f4f4; padding: 10px; border-radius: 3px; font-family: monospace; margin: 10px 0; overflow-x: auto; }
                            .warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; }
                            .success { background: #e8f5e9; padding: 15px; border-left: 4px solid #4caf50; margin: 20px 0; }
                            .nav { margin: 20px 0; }
                            .nav a { display: inline-block; margin: 5px 10px 5px 0; padding: 10px 15px; background: #0066cc; color: white; text-decoration: none; border-radius: 3px; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Setup Guide</h1>
                            <div class="nav">
                                <a href="/">← Home</a>
                                <a href="/checklist?week=2025-W1">Checklist</a>
                                <a href="/navigator">Navigator</a>
                            </div>

                            <div class="success">
                                <strong>Good News:</strong> Since you're reading this, your Z10 is already connected! 
                                You don't need USB cable - everything works via WiFi.
                            </div>

                            <h2>Connection Setup (WiFi Only - No USB Needed)</h2>

                            <div class="step">
                                <span class="step-number">1</span>
                                <strong>Ensure Both Devices on Same WiFi</strong>
                                <p>Your Z10 and laptop must be on the same WiFi network.</p>
                                <div class="code">
                                    On Z10: Settings → Wi-Fi → [Select your WiFi network]
                                </div>
                            </div>

                            <div class="step">
                                <span class="step-number">2</span>
                                <strong>Get Your Laptop's IP Address</strong>
                                <p>On your laptop, run:</p>
                                <div class="code">
                                    ./get-connection-info.sh
                                </div>
                                <p>Or check your network settings. Your IP should be something like: <strong>150.243.213.232</strong></p>
                            </div>

                            <div class="step">
                                <span class="step-number">3</span>
                                <strong>Access from Z10 Browser</strong>
                                <p>On your Z10, open the browser and go to:</p>
                                <div class="code">
                                    http://YOUR_LAPTOP_IP:8080
                                </div>
                                <p>Replace YOUR_LAPTOP_IP with the actual IP from step 2.</p>
                            </div>

                            <div class="step">
                                <span class="step-number">4</span>
                                <strong>Bookmark for Easy Access</strong>
                                <p>On your Z10 browser:</p>
                                <div class="code">
                                    1. Open the page you want to bookmark<br>
                                    2. Menu → Add to Bookmarks<br>
                                    3. Name it (e.g., "BlackBerry Home")
                                </div>
                            </div>

                            <h2>Z10 Browser Settings (Optional Optimization)</h2>

                            <div class="step">
                                <span class="step-number">5</span>
                                <strong>Optimize Browser Settings</strong>
                                <p>On your Z10:</p>
                                <div class="code">
                                    Settings → Browser → Developer Tools → Enable<br>
                                    Settings → Browser → Security → Allow JavaScript (should be on)
                                </div>
                            </div>

                            <div class="step">
                                <span class="step-number">6</span>
                                <strong>Clear Cache if Pages Don't Load</strong>
                                <div class="code">
                                    Browser → Menu → Settings → Privacy and Security → Clear Cache
                                </div>
                            </div>

                            <h2>Using the Apps</h2>

                            <div class="step">
                                <span class="step-number">7</span>
                                <strong>Weekly Checklist</strong>
                                <p>Access your tasks at:</p>
                                <div class="code">
                                    http://YOUR_LAPTOP_IP:8080/checklist?week=2025-W1
                                </div>
                                <p>Checkboxes save automatically using localStorage (survives page refresh).</p>
                            </div>

                            <div class="step">
                                <span class="step-number">8</span>
                                <strong>Resource Navigator</strong>
                                <p>Access curated links at:</p>
                                <div class="code">
                                    http://YOUR_LAPTOP_IP:8080/navigator
                                </div>
                                <p>All links are optimized for BB10 browser compatibility.</p>
                            </div>

                            <div class="warning">
                                <strong>Note:</strong> Your Z10 cannot run Linux or Android. It's locked to BlackBerry 10 OS. 
                                This web-based approach is the best way to extend functionality.
                            </div>

                            <div class="success">
                                <strong>You're All Set!</strong> Everything works via WiFi - no USB cable needed. 
                                Just bookmark the pages you use most on your Z10.
                            </div>
                        </div>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, 200, response, "text/html; charset=utf-8");
        }
    }

    static class TodayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String response = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>Project BlackBerry</title>
                    </head>
                    <body>
                        <h1>Hello from Project BlackBerry</h1>
                        <p>If you see this on your Z10 browser, the pipeline works.</p>
                        <nav>
                            <ul>
                                <li><a href="/">Home</a></li>
                                <li><a href="/checklist?week=2025-W1">Weekly Checklist</a></li>
                                <li><a href="/navigator">Resource Navigator</a></li>
                                <li><a href="/setup">Setup Guide</a></li>
                            </ul>
                        </nav>
                    </body>
                    </html>
                    """;

            sendResponse(exchange, 200, response, "text/html; charset=utf-8");
        }
    }

    static class ImportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String week = getQueryParam(exchange, "week");
            if (week == null || week.isEmpty()) {
                sendError(exchange, 400, "Missing 'week' parameter");
                return;
            }

            try {
                String jsonBody = readRequestBody(exchange);
                WeeklyChecklist checklist = objectMapper.readValue(jsonBody, WeeklyChecklist.class);
                checklists.put(week, checklist);
                
                // Save to disk
                ChecklistStorage.saveChecklist(checklist);
                
                String response = "Imported and saved checklist for week: " + week;
                sendResponse(exchange, 200, response, "text/plain");
                System.out.println("Imported and saved checklist: " + week);
            } catch (Exception e) {
                sendError(exchange, 400, "Invalid JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static class ChecklistHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String week = getQueryParam(exchange, "week");
            if (week == null || week.isEmpty()) {
                week = WeekUtils.getCurrentWeekId(); // Auto-detect current week
            }

            // Try to load from memory, then disk, then create default
            WeeklyChecklist checklist = checklists.get(week);
            if (checklist == null) {
                checklist = ChecklistStorage.loadChecklist(week);
                if (checklist != null) {
                    checklists.put(week, checklist);
                } else {
                    checklist = createDefaultChecklist(week);
                }
            }
            
            String html = renderChecklistHtml(checklist, week);
            sendResponse(exchange, 200, html, "text/html; charset=utf-8");
        }

        private String renderChecklistHtml(WeeklyChecklist checklist, String weekId) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>Weekly Checklist - ").append(checklist.getWeekId()).append("</title>\n");
            html.append("    <style>\n");
            html.append("        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }\n");
            html.append("        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 5px; }\n");
            html.append("        h1 { color: #333; border-bottom: 2px solid #0066cc; padding-bottom: 10px; }\n");
            html.append("        h2 { color: #666; margin-top: 30px; }\n");
            html.append("        .day { margin: 20px 0; padding: 15px; background: #fafafa; border-left: 4px solid #0066cc; }\n");
            html.append("        .task { margin: 10px 0; padding: 10px; background: white; border: 1px solid #ddd; }\n");
            html.append("        .task label { display: block; margin: 5px 0; cursor: pointer; }\n");
            html.append("        .task input[type=\"checkbox\"] { margin-right: 10px; }\n");
            html.append("        .task.completed { opacity: 0.6; text-decoration: line-through; }\n");
            html.append("        .priority { display: inline-block; padding: 2px 8px; border-radius: 3px; font-size: 0.85em; margin-left: 10px; }\n");
            html.append("        .priority.HIGH { background: #ffcccc; color: #cc0000; }\n");
            html.append("        .priority.CRITICAL { background: #ff9999; color: #990000; font-weight: bold; }\n");
            html.append("        .priority.MEDIUM { background: #ffffcc; color: #666600; }\n");
            html.append("        .priority.LOW { background: #ccffcc; color: #006600; }\n");
            html.append("        .time { color: #666; font-size: 0.9em; margin-left: 10px; }\n");
            html.append("        nav { margin: 20px 0; }\n");
            html.append("        nav a { color: #0066cc; text-decoration: none; margin-right: 15px; }\n");
            html.append("        nav a:hover { text-decoration: underline; }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("    <div class=\"container\">\n");
            html.append("        <h1>Weekly Checklist: ").append(checklist.getWeekId()).append("</h1>\n");
            html.append("        <nav>\n");
            html.append("            <a href=\"/\">Home</a>\n");
            html.append("            <a href=\"/navigator\">Resource Navigator</a>\n");
            html.append("            <a href=\"/checklist\">Current Week</a>\n");
            html.append("        </nav>\n");

            // Sort days by date
            List<Map.Entry<String, DayChecklist>> sortedDays = new ArrayList<>(checklist.getDays().entrySet());
            sortedDays.sort(Map.Entry.comparingByKey());

            for (Map.Entry<String, DayChecklist> entry : sortedDays) {
                DayChecklist day = entry.getValue();
                html.append("        <div class=\"day\">\n");
                html.append("            <h2>").append(formatDate(day.getDate())).append("</h2>\n");
                if (day.getPlannedTime() != null && !day.getPlannedTime().isEmpty()) {
                    html.append("            <p><strong>Planned Time:</strong> ").append(day.getPlannedTime()).append("</p>\n");
                }

                for (TaskItem task : day.getTasks()) {
                    String taskId = checklist.getWeekId() + "-" + day.getDate() + "-" + task.getId();
                    String completedClass = task.isCompleted() ? " completed" : "";
                    html.append("            <div class=\"task").append(completedClass).append("\">\n");
                    html.append("                <label>\n");
                    html.append("                    <input type=\"checkbox\" id=\"").append(taskId).append("\"");
                    if (task.isCompleted()) {
                        html.append(" checked");
                    }
                    html.append(" onchange=\"saveTaskState('").append(taskId).append("', this.checked)\">\n");
                    html.append("                    ").append(escapeHtml(task.getDescription())).append("\n");
                    if (task.getPriority() != null && !task.getPriority().isEmpty()) {
                        html.append("                    <span class=\"priority ").append(task.getPriority()).append("\">").append(task.getPriority()).append("</span>\n");
                    }
                    if (task.getEstimatedTime() != null && !task.getEstimatedTime().isEmpty()) {
                        html.append("                    <span class=\"time\">(").append(task.getEstimatedTime()).append(")</span>\n");
                    }
                    html.append("                </label>\n");
                    html.append("            </div>\n");
                }
                html.append("        </div>\n");
            }

            // Display resources section
            if (checklist.getResources() != null && !checklist.getResources().isEmpty()) {
                html.append("        <div class=\"day\" style=\"margin-top: 40px;\">\n");
                html.append("            <h2>Resources for This Week</h2>\n");
                html.append("            <ul style=\"list-style-type: none; padding: 0;\">\n");
                for (var resource : checklist.getResources()) {
                    html.append("                <li style=\"margin: 10px 0; padding: 10px; background: white; border: 1px solid #ddd;\">\n");
                    html.append("                    <a href=\"").append(escapeHtml(resource.getUrl())).append("\" style=\"color: #0066cc; text-decoration: none; font-weight: bold;\">");
                    html.append(escapeHtml(resource.getTitle())).append("</a>\n");
                    if (resource.getCategory() != null && !resource.getCategory().isEmpty()) {
                        html.append("                    <span style=\"color: #666; font-size: 0.9em; margin-left: 10px;\">(");
                        html.append(escapeHtml(resource.getCategory())).append(")</span>\n");
                    }
                    html.append("                </li>\n");
                }
                html.append("            </ul>\n");
                html.append("        </div>\n");
            }

            html.append("    </div>\n");
            html.append("    <script>\n");
            html.append("        // Load saved checkbox states from localStorage\n");
            html.append("        function loadTaskStates() {\n");
            html.append("            var checkboxes = document.querySelectorAll('input[type=\"checkbox\"]');\n");
            html.append("            checkboxes.forEach(function(cb) {\n");
            html.append("                var saved = localStorage.getItem(cb.id);\n");
            html.append("                if (saved === 'true') {\n");
            html.append("                    cb.checked = true;\n");
            html.append("                    cb.closest('.task').classList.add('completed');\n");
            html.append("                }\n");
            html.append("            });\n");
            html.append("        }\n");
            html.append("        \n");
            html.append("        // Save checkbox state to localStorage\n");
            html.append("        function saveTaskState(taskId, completed) {\n");
            html.append("            localStorage.setItem(taskId, completed ? 'true' : 'false');\n");
            html.append("            var taskDiv = document.getElementById(taskId).closest('.task');\n");
            html.append("            if (completed) {\n");
            html.append("                taskDiv.classList.add('completed');\n");
            html.append("            } else {\n");
            html.append("                taskDiv.classList.remove('completed');\n");
            html.append("            }\n");
            html.append("        }\n");
            html.append("        \n");
            html.append("        // Load states when page loads\n");
            html.append("        window.onload = loadTaskStates;\n");
            html.append("    </script>\n");
            html.append("</body>\n");
            html.append("</html>\n");

            return html.toString();
        }

        private String formatDate(String dateStr) {
            try {
                // Parse YYYY-MM-DD and format nicely
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    String[] months = {"", "January", "February", "March", "April", "May", "June",
                                      "July", "August", "September", "October", "November", "December"};
                    String[] weekdays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    // Simple weekday calculation (Zeller's congruence approximation)
                    java.time.LocalDate date = java.time.LocalDate.of(year, month, day);
                    return weekdays[date.getDayOfWeek().getValue() % 7] + ", " + months[month] + " " + day + ", " + year;
                }
            } catch (Exception e) {
                // Fall through
            }
            return dateStr;
        }

        private String escapeHtml(String text) {
            if (text == null) return "";
            return text.replace("&", "&amp;")
                      .replace("<", "&lt;")
                      .replace(">", "&gt;")
                      .replace("\"", "&quot;")
                      .replace("'", "&#39;");
        }

        private WeeklyChecklist createDefaultChecklist(String weekId) {
            Map<String, DayChecklist> days = new HashMap<>();
            return new WeeklyChecklist(weekId, days, new ArrayList<>());
        }
    }

    static class NavigatorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }

            String html = renderNavigatorHtml();
            sendResponse(exchange, 200, html, "text/html; charset=utf-8");
        }

        private String renderNavigatorHtml() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("    <meta charset=\"UTF-8\">\n");
            html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("    <title>BlackBerry Navigator</title>\n");
            html.append("    <style>\n");
            html.append("        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }\n");
            html.append("        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 5px; }\n");
            html.append("        h1 { color: #333; border-bottom: 2px solid #0066cc; padding-bottom: 10px; }\n");
            html.append("        h2 { color: #666; margin-top: 30px; margin-bottom: 15px; }\n");
            html.append("        ul { list-style-type: none; padding: 0; }\n");
            html.append("        li { margin: 10px 0; padding: 10px; background: #fafafa; border-left: 3px solid #0066cc; }\n");
            html.append("        a { color: #0066cc; text-decoration: none; font-weight: bold; }\n");
            html.append("        a:hover { text-decoration: underline; }\n");
            html.append("        .description { color: #666; font-size: 0.9em; margin-top: 5px; }\n");
            html.append("        nav { margin: 20px 0; }\n");
            html.append("        nav a { margin-right: 15px; }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("    <div class=\"container\">\n");
            html.append("        <h1>Essential Resources</h1>\n");
            html.append("        <nav>\n");
            html.append("            <a href=\"/checklist?week=2025-W1\">Weekly Checklist</a>\n");
            html.append("            <a href=\"/today\">Home</a>\n");
            html.append("        </nav>\n");

            // Coding Resources
            html.append("        <h2>Code</h2>\n");
            html.append("        <ul>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=java+spring+boot\">DuckDuckGo HTML (Java Search)</a>\n");
            html.append("                <div class=\"description\">Lightweight search - works on BB10</div></li>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=stackoverflow+java\">DuckDuckGo HTML (Stack Overflow)</a>\n");
            html.append("                <div class=\"description\">Search Stack Overflow via DuckDuckGo</div></li>\n");
            html.append("            <li><a href=\"https://docs.oracle.com/javase/17/docs/api/index.html\">Java 17 API Docs</a>\n");
            html.append("                <div class=\"description\">Official Java documentation</div></li>\n");
            html.append("        </ul>\n");

            // University Resources
            html.append("        <h2>College</h2>\n");
            html.append("        <ul>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=cs+algorithms+pdf\">PDF Search (Algorithms)</a>\n");
            html.append("                <div class=\"description\">Search for algorithm PDFs</div></li>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=computer+science+lecture+notes\">Lecture Notes Search</a>\n");
            html.append("                <div class=\"description\">Find CS lecture materials</div></li>\n");
            html.append("        </ul>\n");

            // Reference Resources
            html.append("        <h2>Reference</h2>\n");
            html.append("        <ul>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=regex+cheat+sheet\">Regex Reference</a>\n");
            html.append("                <div class=\"description\">Regular expressions guide</div></li>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=git+commands+cheat+sheet\">Git Commands</a>\n");
            html.append("                <div class=\"description\">Quick Git reference</div></li>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=maven+commands\">Maven Reference</a>\n");
            html.append("                <div class=\"description\">Maven build tool help</div></li>\n");
            html.append("        </ul>\n");

            // Documentation
            html.append("        <h2>Documentation</h2>\n");
            html.append("        <ul>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=spring+boot+documentation\">Spring Boot Docs</a>\n");
            html.append("                <div class=\"description\">Spring Boot framework docs</div></li>\n");
            html.append("            <li><a href=\"https://html.duckduckgo.com/?q=jackson+json+java\">Jackson JSON</a>\n");
            html.append("                <div class=\"description\">JSON processing library</div></li>\n");
            html.append("        </ul>\n");

            html.append("    </div>\n");
            html.append("</body>\n");
            html.append("</html>\n");

            return html.toString();
        }
    }

    // Utility methods
    private static String getQueryParam(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        String[] params = query.split("&");
        for (String param : params) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(paramName)) {
                try {
                    return java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendResponse(exchange, statusCode, "Error: " + message, "text/plain");
    }
}
