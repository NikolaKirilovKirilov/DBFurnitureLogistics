package  org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class FurnitureLogisticsApp extends Application {

    // Настройки за връзка с базата (от предишния скрипт)
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/FurnitureLogisticsDB";
//    private static final String USER = "logistics_user";
//    private static final String PASS = "password123";
    // Change these lines
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=FurnitureLogisticsDB;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "logistics_user"; // Usually 'sa' or a user you created
    private static final String PASS = "Password123!";

    private Connection connection;
    private TableView<ObservableList<String>> tableView = new TableView<>();
    private ComboBox<String> tableSelector = new ComboBox<>();
    private Label statusLabel = new Label("Готов за работа");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectToDB();

        // --- ГОРНА ЧАСТ: Избор на таблица ---
        Label lblSelect = new Label("Избери таблица:");
        // Списък с таблиците от схемата
        tableSelector.getItems().addAll(
                "Klient", "Potrebitel", "Ofis", "Sklad", "Dostavchik",
                "Produkt", "Mebel", "Profil", "Poryachka", "Dostavka", "Plashtane"
        );
        tableSelector.setOnAction(e -> loadTableData(tableSelector.getValue()));

        HBox topPanel = new HBox(10, lblSelect, tableSelector);
        topPanel.setPadding(new Insets(10));

        // --- ЦЕНТЪР: Таблица с данни ---
        tableView.setPlaceholder(new Label("Няма данни за показване"));

        // --- ДЯСНА ЧАСТ: Операции (CRUD) ---
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: #f0f0f0;");

        Label lblOps = new Label("Операции:");
        lblOps.setStyle("-fx-font-weight: bold");

        // INSERT
        TextField txtInsertValues = new TextField();
        txtInsertValues.setPromptText("'Stoina', 'Adres'...");
        Button btnAdd = new Button("Добави ред");
        Label lblHint = new Label("(Стойности без ID, със запетаи)");
        lblHint.setStyle("-fx-font-size: 10px;");

        btnAdd.setOnAction(e -> insertRecord(txtInsertValues.getText()));

        // DELETE
        Button btnDelete = new Button("Изтрий избран ред");
        btnDelete.setStyle("-fx-text-fill: red;");
        btnDelete.setOnAction(e -> deleteSelectedRecord());

        // REFRESH
        Button btnRefresh = new Button("Обнови данните");
        btnRefresh.setOnAction(e -> loadTableData(tableSelector.getValue()));

        rightPanel.getChildren().addAll(lblOps, new Label("Стойности за нов ред:"), txtInsertValues, lblHint, btnAdd, new Separator(), btnDelete, new Separator(), btnRefresh);

        // --- ДОЛНА ЧАСТ: Сложни справки (JOINs) ---
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(10));
        Label lblReports = new Label("Справки (JOIN Queries):");
        lblReports.setStyle("-fx-font-weight: bold");

        HBox reportButtons = new HBox(10);

        // Справка 1: Поръчки с имена на клиенти (Klient -> Poryachka)
        Button btnReport1 = new Button("Поръчки и Клиенти");
        btnReport1.setOnAction(e -> executeQuery(
                "SELECT P.ID as PoryachkaID, P.Data, K.Ime as Klient, K.Adres " +
                        "FROM Poryachka P " +
                        "JOIN Klient K ON P.Klient_ID = K.ID"
        ));

        // Справка 2: Наличности в складовете (Sklad -> Sklad_Mebel -> Mebel)
        Button btnReport2 = new Button("Наличност Мебели");
        btnReport2.setOnAction(e -> executeQuery(
                "SELECT S.Mestopolozhenie, M.Kategoria, SM.Nalichnost " +
                        "FROM Sklad_Mebel SM " +
                        "JOIN Sklad S ON SM.Sklad_ID = S.ID " +
                        "JOIN Mebel M ON SM.Mebel_ID = M.ID"
        ));

        // Справка 3: Доставки с шофьор и дестинация (Dostavka -> Dostavchik)
        Button btnReport3 = new Button("График Доставки");
        btnReport3.setOnAction(e -> executeQuery(
                "SELECT D.ID, D.Data, Dost.Raion as Dostavchik_Raion, D.Adres_Dostavka " +
                        "FROM Dostavka D " +
                        "JOIN Dostavchik Dost ON D.Dostavchik_ID = Dost.ID"
        ));

        reportButtons.getChildren().addAll(btnReport1, btnReport2, btnReport3);
        bottomPanel.getChildren().addAll(lblReports, reportButtons, statusLabel);

        // --- СГЛОБЯВАНЕ ---
        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(tableView);
        root.setRight(rightPanel);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Логистична Система - Database Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- ЛОГИКА ЗА БАЗА ДАННИ ---

    private void connectToDB() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statusLabel.setText("Статус: Свързан към базата данни.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            statusLabel.setText("Грешка при връзка: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    // Зарежда цялата таблица (SELECT * FROM table)
    private void loadTableData(String tableName) {
        if (tableName == null || tableName.isEmpty()) return;
        String sql = "SELECT * FROM " + tableName;
        executeQuery(sql);
    }

    // Изпълнява SELECT заявка и пълни TableView динамично
    private void executeQuery(String sql) {
        try {
            if (connection == null || connection.isClosed()) connectToDB();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // 1. Изчистване на старата структура
            tableView.getColumns().clear();
            tableView.getItems().clear();

            // 2. Създаване на колони динамично спрямо Metadata
            int colCount = rs.getMetaData().getColumnCount();
            for (int i = 0; i < colCount; i++) {
                final int j = i;
                String colName = rs.getMetaData().getColumnName(i + 1);
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(colName);

                // Магията за връзване на данни към колоната
                col.setCellValueFactory(param ->
                        new SimpleStringProperty(param.getValue().get(j))
                );
                tableView.getColumns().add(col);
            }

            // 3. Пълнене на данните
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= colCount; i++) {
                    // Взимаме всичко като String за простота
                    String val = rs.getString(i);
                    row.add(val == null ? "NULL" : val);
                }
                data.add(row);
            }
            tableView.setItems(data);
            statusLabel.setText("Заявката изпълнена успешно.");

        } catch (Exception e) {
            statusLabel.setText("Грешка при заявка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Изтрива избран ред (предполага, че първата колона е ID)
    private void deleteSelectedRecord() {
        String currentTable = tableSelector.getValue();
        ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();

        if (currentTable == null || selectedRow == null) {
            showAlert("Грешка", "Моля изберете таблица и ред за изтриване.");
            return;
        }

        // Взимаме ID от първата колона (индекс 0)
        String idToDelete = selectedRow.get(0);
        String sql = "DELETE FROM " + currentTable + " WHERE ID = " + idToDelete;

        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            statusLabel.setText("Ред с ID " + idToDelete + " е изтрит.");
            loadTableData(currentTable); // Рефреш
        } catch (SQLException e) {
            showAlert("SQL Грешка", e.getMessage());
        }
    }

    // Добавя нов ред (опростено: потребителят въвежда стойностите)
//    private void insertRecord(String values) {
//        String currentTable = tableSelector.getValue();
//        if (currentTable == null || values.isEmpty()) {
//            showAlert("Грешка", "Изберете таблица и въведете стойности.");
//            return;
//        }
//
//        // Потребителят трябва да въведе: 'Иван', 'София' (със скобите)
//        // Ние добавяме NULL за ID-то (Auto Increment)
//        String sql = "INSERT INTO " + currentTable + " VALUES (NULL, " + values + ")";
//
//        try {
//            Statement stmt = connection.createStatement();
//            stmt.executeUpdate(sql);
//            statusLabel.setText("Успешно добавен ред.");
//            loadTableData(currentTable); // Рефреш
//        } catch (SQLException e) {
//            showAlert("SQL Грешка", "Уверете се, че въвеждате данните в точния ред и формат (String в кавички).\n" + e.getMessage());
//        }
//    }

    private void insertRecord(String values) {
        String currentTable = tableSelector.getValue();
        if (currentTable == null || values.isEmpty()) {
            showAlert("Грешка", "Изберете таблица и въведете стойности.");
            return;
        }

        try {
            String columns = getInsertColumns(currentTable);
            String sql = "INSERT INTO " + currentTable +
                    " (" + columns + ") VALUES (" + values + ")";

            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);

            statusLabel.setText("Успешно добавен ред.");
            loadTableData(currentTable);

        } catch (Exception e) {
            showAlert(
                    "SQL Грешка",
                    "Проверете реда и формата на данните.\n" + e.getMessage()
            );
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getInsertColumns(String tableName) {
        switch (tableName) {
            case "Klient":
                return "Adres, Ime";
            case "Potrebitel":
                return "Ime, Email, Telefon";
            case "Ofis":
                return "Otdel, Rakovoditel";
            case "Sklad":
                return "Mestopolozhenie, Kapacitet";
            case "Dostavchik":
                return "Raion, Zaplata";
            case "Produkt":
                return "Ime_Produkt, Cena";
            case "Mebel":
                return "Kategoria";
            case "Profil":
                return "Material";
            case "Poryachka":
                return "Data, Klient_ID, Ofis_ID";
            case "Plashtane":
                return "Banka, Metod, Poryachka_ID";
            case "Dostavka":
                return "Adres_Dostavka, Data, Klient_ID, Dostavchik_ID, Sklad_ID";
            default:
                throw new IllegalArgumentException(
                        "INSERT не е дефиниран за таблица: " + tableName
                );
        }
    }

}