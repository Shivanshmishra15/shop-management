package com.shopapp.ui;

import com.shopapp.database.ProductDAO;
import com.shopapp.database.PurchaseOrderDAO;
import com.shopapp.database.SupplierDAO;
import com.shopapp.models.Product;
import com.shopapp.models.PurchaseOrder;
import com.shopapp.models.PurchaseOrderItem;
import com.shopapp.models.Supplier;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PurchaseOrderView extends TabPane {

    private PurchaseOrderDAO poDAO;
    private SupplierDAO supplierDAO;
    private ProductDAO productDAO;

    // Create Order Tab Controls
    private ComboBox<Supplier> cmbSupplier;
    private TableView<Product> productTable;
    private TableView<PurchaseOrderItem> itemTable;
    private ObservableList<PurchaseOrderItem> orderItems = FXCollections.observableArrayList();
    private Label lblTotal;

    // View Orders Tab Controls
    private TableView<PurchaseOrder> historyTable;

    public PurchaseOrderView() {
        poDAO = new PurchaseOrderDAO();
        supplierDAO = new SupplierDAO();
        productDAO = new ProductDAO();

        Tab tabCreate = new Tab("Create Purchase Order", createOrderView());
        tabCreate.setClosable(false);
        
        Tab tabHistory = new Tab("Order History", createHistoryView());
        tabHistory.setClosable(false);
        tabHistory.setOnSelectionChanged(e -> {
            if (tabHistory.isSelected()) loadHistory();
        });

        getTabs().addAll(tabCreate, tabHistory);
    }

    private VBox createOrderView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        // Header
        Label title = new Label("New Purchase Order");
        title.getStyleClass().add("page-title");

        // Info Section
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        
        cmbSupplier = new ComboBox<>();
        cmbSupplier.setPromptText("Select Supplier");
        cmbSupplier.setPrefWidth(250);
        loadSuppliers();
        // Custom converter to show name
        cmbSupplier.setConverter(new StringConverter<>() {
            @Override public String toString(Supplier s) { return s == null ? "" : s.getName(); }
            @Override public Supplier fromString(String string) { return null; }
        });

        infoBox.getChildren().addAll(new Label("Supplier:"), cmbSupplier);

        // Content Split
        HBox content = new HBox(20);
        VBox.setVgrow(content, Priority.ALWAYS);

        // Left: Product Selection
        VBox left = new VBox(10);
        HBox.setHgrow(left, Priority.ALWAYS);
        left.getChildren().add(new Label("Select Products to Order"));
        
        productTable = new TableView<>();
        TableColumn<Product, String> pName = new TableColumn<>("Product");
        pName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Product, Integer> pStock = new TableColumn<>("Current Stock");
        pStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        
        productTable.getColumns().addAll(pName, pStock);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadProducts(); // Sync load for now
        
        // Add Button
        Button btnAdd = new Button("Add to Order");
        btnAdd.setOnAction(e -> addToOrder());

        left.getChildren().addAll(productTable, btnAdd);

        // Right: Order Items
        VBox right = new VBox(10);
        HBox.setHgrow(right, Priority.ALWAYS);
        right.getChildren().add(new Label("Order Items"));

        itemTable = new TableView<>();
        itemTable.setItems(orderItems);
        
        TableColumn<PurchaseOrderItem, String> iName = new TableColumn<>("Product");
        iName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        
        TableColumn<PurchaseOrderItem, Integer> iQty = new TableColumn<>("Qty");
        iQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<PurchaseOrderItem, Double> iCost = new TableColumn<>("Cost/Unit");
        iCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        
        itemTable.getColumns().addAll(iName, iQty, iCost);
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        lblTotal = new Label("Total: 0.00");
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Button btnSubmit = new Button("Submit Order");
        btnSubmit.getStyleClass().add("button-primary");
        btnSubmit.setOnAction(e -> submitOrder());

        right.getChildren().addAll(itemTable, lblTotal, btnSubmit);

        content.getChildren().addAll(left, right);
        root.getChildren().addAll(title, infoBox, content);
        return root;
    }

    private VBox createHistoryView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        
        historyTable = new TableView<>();
        
        TableColumn<PurchaseOrder, Integer> idCol = new TableColumn<>("PO #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<PurchaseOrder, String> supCol = new TableColumn<>("Supplier");
        supCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        
        TableColumn<PurchaseOrder, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ));
        
        TableColumn<PurchaseOrder, Double> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        
        TableColumn<PurchaseOrder, String> statCol = new TableColumn<>("Status");
        statCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        historyTable.getColumns().addAll(idCol, supCol, dateCol, amtCol, statCol);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        root.getChildren().addAll(new Label("Purchase Order History"), historyTable);
        return root;
    }

    private void loadSuppliers() {
        try {
            cmbSupplier.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadProducts() {
        try {
            productTable.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void loadHistory() {
        try {
            historyTable.setItems(FXCollections.observableArrayList(poDAO.getAllOrders()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addToOrder() {
        Product p = productTable.getSelectionModel().getSelectedItem();
        if (p == null) return;
        
        // Simple input dialog for Qty
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Quantity");
        dialog.setHeaderText("Enter quantity for " + p.getName());
        dialog.setContentText("Quantity:");
        
        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                // Assume Cost Price is same as pricePerUnit for simplicity, or ask user?
                // Let's ask for Cost Price too? Keeping it simple: take current price as default cost
                double cost = p.getPricePerUnit(); 
                
                PurchaseOrderItem item = new PurchaseOrderItem(0, 0, p.getId(), qty, cost);
                item.setProductName(p.getName());
                orderItems.add(item);
                updateTotal();
            } catch (NumberFormatException e) { /* ignore */ }
        });
    }

    private void updateTotal() {
        double sum = orderItems.stream().mapToDouble(PurchaseOrderItem::getTotal).sum();
        lblTotal.setText(String.format("Total: %.2f", sum));
    }

    private void submitOrder() {
        Supplier s = cmbSupplier.getValue();
        if (s == null) {
            new Alert(Alert.AlertType.ERROR, "Select Supplier").show();
            return;
        }
        if (orderItems.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Add items").show();
            return;
        }

        try {
            PurchaseOrder po = new PurchaseOrder(0, s.getId(), null, "PENDING", 
                orderItems.stream().mapToDouble(PurchaseOrderItem::getTotal).sum());
            po.setItems(new ArrayList<>(orderItems));
            
            poDAO.createPurchaseOrder(po);
            
            new Alert(Alert.AlertType.INFORMATION, "Order Created Successfully").show();
            orderItems.clear();
            updateTotal();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed: " + e.getMessage()).show();
        }
    }
}
