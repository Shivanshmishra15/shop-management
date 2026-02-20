package com.shopapp.ui;

import com.shopapp.database.ExpenseDAO;
import com.shopapp.database.FinanceDAO;
import com.shopapp.models.Expense;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;

public class FinanceView extends BorderPane {

    private final FinanceDAO financeDAO;
    private final ExpenseDAO expenseDAO;
    private TableView<Expense> expenseTable;
    
    // Labels
    private Label lblRevenue, lblCOGS, lblExpenses, lblProfit;

    public FinanceView() {
        financeDAO = new FinanceDAO();
        expenseDAO = new ExpenseDAO();
        
        getStyleClass().add("finance-view");
        setPadding(new Insets(20));
        
        // Top: Dashboard Cards
        HBox dashboard = createDashboardStats();
        
        // Center: Expense Manager
        VBox expenseManager = createExpenseManager();
        
        setTop(dashboard);
        setCenter(expenseManager);
        
        refreshStats();
    }

    private HBox createDashboardStats() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 0, 20, 0));
        
        lblRevenue = createStatLabel("Total Revenue", "0.00", "#10b981");
        lblCOGS = createStatLabel("PO Cost (COGS)", "0.00", "#f59e0b");
        lblExpenses = createStatLabel("Op. Expenses", "0.00", "#f43f5e");
        lblProfit = createStatLabel("Net Profit", "0.00", "#3b82f6");
        
        box.getChildren().addAll(lblRevenue.getParent(), lblCOGS.getParent(), lblExpenses.getParent(), lblProfit.getParent());
        return box;
    }
    
    private Label createStatLabel(String title, String value, String color) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPrefWidth(250);
        card.setAlignment(Pos.CENTER);
        
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        
        Label lblValue = new Label("₹" + value);
        lblValue.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(lblTitle, lblValue);
        return lblValue; // Return value label reference to update later
    }

    private VBox createExpenseManager() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("card");
        
        Label title = new Label("Expense Manager");
        title.getStyleClass().add("card-title");

        // Input Form
        HBox form = new HBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> cmbCat = new ComboBox<>();
        cmbCat.setItems(FXCollections.observableArrayList("Rent", "Utilities", "Salary", "Maintenance", "Misc"));
        cmbCat.setPromptText("Category");
        
        TextField txtDesc = new TextField(); txtDesc.setPromptText("Description");
        TextField txtAmount = new TextField(); txtAmount.setPromptText("Amount");
        
        Button btnAdd = new Button("Add Expense");
        btnAdd.getStyleClass().add("button-primary");
        btnAdd.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(txtAmount.getText());
                String cat = cmbCat.getValue();
                if (cat == null) cat = "Misc";
                
                expenseDAO.addExpense(new Expense(0, cat, txtDesc.getText(), amt, null));
                refreshStats();
                loadExpenses();
                txtAmount.clear(); txtDesc.clear();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid Input").show();
            }
        });
        
        form.getChildren().addAll(cmbCat, txtDesc, txtAmount, btnAdd);

        // Table
        expenseTable = new TableView<>();
        
        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ));
        
        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        TableColumn<Expense, Double> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        expenseTable.getColumns().addAll(dateCol, catCol, descCol, amtCol);
        expenseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        loadExpenses();
        
        root.getChildren().addAll(title, form, expenseTable);
        VBox.setVgrow(expenseTable, Priority.ALWAYS);
        return root;
    }

    private void refreshStats() {
        try {
            double revenue = financeDAO.getTotalSales();
            double cogs = financeDAO.getTotalPurchaseCosts();
            double expenses = financeDAO.getTotalExpenses();
            double profit = revenue - (cogs + expenses);
            
            lblRevenue.setText(String.format("₹%.2f", revenue));
            lblCOGS.setText(String.format("₹%.2f", cogs));
            lblExpenses.setText(String.format("₹%.2f", expenses));
            lblProfit.setText(String.format("₹%.2f", profit));
        } catch (java.sql.SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Error loading financial data: " + e.getMessage()).show();
        }
    }
    
    private void loadExpenses() {
        try {
            expenseTable.setItems(FXCollections.observableArrayList(expenseDAO.getAllExpenses()));
        } catch (Exception e) { e.printStackTrace(); }
    }
}
