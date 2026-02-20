package com.shopapp.ui;

import com.shopapp.database.ProductDAO;
import com.shopapp.models.CartItem;
import com.shopapp.models.Product;
import com.shopapp.utils.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BillingView extends HBox {
    private static final Logger logger = LoggerFactory.getLogger(BillingView.class);
    private final ProductDAO productDAO;
    private final ObservableList<Product> availableProducts = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final Map<String, List<CartItem>> heldBills = new HashMap<>();

    // UI Controls
    private TableView<Product> productTable;
    private TableView<CartItem> cartTable;
    private TextField txtSearch;
    private Label lblTotal;
    private TextField txtCustomer;
    private TextField txtContact;
    private TextField txtGst;
    private ProgressIndicator loadingIndicator;
    private MenuButton btnUnhold;

    public BillingView() {
        this.productDAO = new ProductDAO();
        setSpacing(20);
        setPadding(new Insets(20));

        // --- Left Side: Product Search ---
        VBox leftPane = new VBox(10);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblSearch = new Label("Product Catalog");
        lblSearch.getStyleClass().add("page-title");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);
        header.getChildren().addAll(lblSearch, loadingIndicator);
        
        txtSearch = new TextField();
        txtSearch.setPromptText("Search by Name or Barcode...");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterProducts(newVal));
        
        // Auto-focus search on load
        Platform.runLater(() -> txtSearch.requestFocus());

        txtSearch.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.DOWN) {
                productTable.requestFocus();
                productTable.getSelectionModel().select(0);
            } else if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleBarcodeScan(txtSearch.getText().trim());
            }
        });

        productTable = new TableView<>();
        setupProductTable();
        // Double click to add
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) addToCart(row.getItem());
            });
            return row;
        });

        // Enter key to add product
        productTable.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                Product selected = productTable.getSelectionModel().getSelectedItem();
                if (selected != null) addToCart(selected);
                event.consume();
            }
        });

        leftPane.getChildren().addAll(header, txtSearch, productTable);
        loadProducts();

        // --- Right Side: Cart & Checkout ---
        VBox rightPane = new VBox(15);
        rightPane.setPrefWidth(500);
        rightPane.getStyleClass().add("card");

        HBox cartHeader = new HBox(10);
        cartHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblCart = new Label("Current Bill");
        lblCart.getStyleClass().add("card-title");
        lblCart.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        btnUnhold = new MenuButton("Held Bills");
        btnUnhold.getStyleClass().add("button-warning");
        btnUnhold.setVisible(false);

        cartHeader.getChildren().addAll(lblCart, spacer, btnUnhold);

        // Customer Info
        GridPane custGrid = new GridPane();
        custGrid.setHgap(10);
        custGrid.setVgap(10);
        txtCustomer = new TextField(); txtCustomer.setPromptText("Name");
        txtContact = new TextField(); txtContact.setPromptText("Contact");
        txtGst = new TextField(); txtGst.setPromptText("GST No (Optional)");
        
        Label l1 = new Label("Customer:"); l1.getStyleClass().add("label");
        Label l2 = new Label("Phone:"); l2.getStyleClass().add("label");
        Label l3 = new Label("GST:"); l3.getStyleClass().add("label");

        custGrid.addRow(0, l1, txtCustomer);
        custGrid.addRow(1, l2, txtContact);
        custGrid.addRow(2, l3, txtGst);

        // Add Undo/Redo tooltips
        addShortcutsTooltip(txtCustomer);
        addShortcutsTooltip(txtContact);
        addShortcutsTooltip(txtGst);

        // Cart Table
        cartTable = new TableView<>();
        cartTable.setItems(cartItems);
        setupCartTable();
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        // Delete key to remove from cart
        cartTable.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.DELETE || event.getCode() == javafx.scene.input.KeyCode.BACK_SPACE) {
                removeFromCart();
            }
        });

        // Totals
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        lblTotal = new Label("Total: 0.00");
        lblTotal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        totalBox.getChildren().add(lblTotal);

        // Actions
        HBox actions = new HBox(10);
        Button btnCheckout = new Button("Checkout");
        btnCheckout.getStyleClass().add("button-primary");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCheckout, Priority.ALWAYS);
        btnCheckout.setOnAction(e -> handleCheckout());
        
        Button btnHold = new Button("Hold");
        btnHold.getStyleClass().add("button-warning");
        btnHold.setOnAction(e -> handleHoldBill());
        
        Button btnRemove = new Button("Remove");
        btnRemove.getStyleClass().add("button-danger");
        btnRemove.setOnAction(e -> removeFromCart());

        actions.getChildren().addAll(btnCheckout, btnHold, btnRemove);

        rightPane.getChildren().addAll(cartHeader, custGrid, cartTable, totalBox, actions);

        getChildren().addAll(leftPane, rightPane);

        // View-level shortcuts
        this.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                if (event.getCode() == javafx.scene.input.KeyCode.S || event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    handleCheckout();
                } else if (event.getCode() == javafx.scene.input.KeyCode.H) {
                    handleHoldBill();
                } else if (event.getCode() == javafx.scene.input.KeyCode.F) {
                    txtSearch.requestFocus();
                }
            } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                txtSearch.requestFocus();
                txtSearch.selectAll();
            }
        });
    }
    
    // ... (Keep existing setupProductTable, setupCartTable, loadProducts, filterProducts, addToCart, removeFromCart, updateTotal)
    // I need to replicate them here or use replace chunks. 
    // Since I am rewriting logic for Hold Bill, rewriting the whole file is cleaner to avoid structural mismatch.

     private void setupProductTable() {
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        
        TableColumn<Product, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnAdd = new Button("Add");
            {
                btnAdd.getStyleClass().add("button-primary");
                btnAdd.setStyle("-fx-padding: 3 10; -fx-font-size: 11px;"); 
                btnAdd.setOnAction(event -> {
                    Product p = getTableView().getItems().get(getIndex());
                    addToCart(p);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null); else setGraphic(btnAdd);
            }
        });

        productTable.getColumns().addAll(nameCol, priceCol, stockCol, actionCol);
        
        nameCol.prefWidthProperty().bind(productTable.widthProperty().multiply(0.4));
        priceCol.prefWidthProperty().bind(productTable.widthProperty().multiply(0.2));
        stockCol.prefWidthProperty().bind(productTable.widthProperty().multiply(0.2));
        actionCol.prefWidthProperty().bind(productTable.widthProperty().multiply(0.19));
        
        productTable.setItems(availableProducts);
    }

    private void setupCartTable() {
        TableColumn<CartItem, String> nameCol = new TableColumn<>("Item");
        nameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        
        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setCellFactory(param -> new TableCell<>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Label lblQty = new Label();
            private final HBox container = new HBox(8);
            
            {
                btnMinus.getStyleClass().add("button-warning");
                btnMinus.setStyle("-fx-padding: 0 8; -fx-min-width: 25px; -fx-font-weight: bold;");
                btnPlus.getStyleClass().add("button-primary");
                btnPlus.setStyle("-fx-padding: 0 8; -fx-min-width: 25px; -fx-font-weight: bold;");
                lblQty.setStyle("-fx-font-weight: bold; -fx-min-width: 20px; -fx-alignment: center;");
                
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(btnMinus, lblQty, btnPlus);
                
                btnPlus.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() < item.getProduct().getCurrentStock()) {
                        item.setQuantity(item.getQuantity() + 1);
                        getTableView().refresh();
                        updateTotal();
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Not enough stock!").show();
                    }
                });
                
                btnMinus.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        getTableView().refresh();
                        updateTotal();
                    }
                });
            }
            
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    CartItem cartItem = (CartItem) getTableRow().getItem();
                    lblQty.setText(String.valueOf(cartItem.getQuantity()));
                    setGraphic(container);
                }
            }
        });
        
        TableColumn<CartItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        cartTable.getColumns().addAll(nameCol, qtyCol, totalCol);
        
        nameCol.prefWidthProperty().bind(cartTable.widthProperty().multiply(0.4));
        qtyCol.prefWidthProperty().bind(cartTable.widthProperty().multiply(0.35));
        totalCol.prefWidthProperty().bind(cartTable.widthProperty().multiply(0.24));
    }

    private void loadProducts() {
        loadingIndicator.setVisible(true);
        Task<List<Product>> task = new Task<>() {
            @Override protected List<Product> call() throws Exception { return productDAO.getAllProducts(); }
        };
        task.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            availableProducts.setAll(task.getValue());
        });
        task.setOnFailed(e -> loadingIndicator.setVisible(false));
        new Thread(task).start();
    }

    private void filterProducts(String query) {
        if (query == null || query.isEmpty()) { loadProducts(); return; }
        String lowerQuery = query.toLowerCase();
        Task<List<Product>> task = new Task<>() {
            @Override protected List<Product> call() throws Exception {
                return productDAO.getAllProducts().stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerQuery) || (p.getBarcode() != null && p.getBarcode().toLowerCase().contains(lowerQuery)))
                    .collect(Collectors.toList());
            }
        };
        task.setOnSucceeded(e -> availableProducts.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void addToCart(Product p) {
        if (p.getCurrentStock() <= 0) { new Alert(Alert.AlertType.WARNING, "Out of Stock!").show(); return; }
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == p.getId()) {
                if (item.getQuantity() < p.getCurrentStock()) {
                    item.setQuantity(item.getQuantity() + 1);
                    cartTable.refresh();
                    updateTotal();
                } else { new Alert(Alert.AlertType.WARNING, "Not enough stock!").show(); }
                return;
            }
        }
        cartItems.add(new CartItem(p, 1));
        updateTotal();
    }
    
    private void removeFromCart() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) { cartItems.remove(selected); updateTotal(); }
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        lblTotal.setText(String.format("Total: %.2f", total));
    }

    private void handleHoldBill() {
        if (cartItems.isEmpty()) return;
        
        TextInputDialog dialog = new TextInputDialog("Customer 1");
        dialog.setTitle("Hold Bill");
        dialog.setHeaderText("Enter identifier for this bill");
        dialog.setContentText("Name/ID:");
        
        dialog.showAndWait().ifPresent(name -> {
            heldBills.put(name + " (" + LocalDateTime.now().getMinute() + "m)", new ArrayList<>(cartItems));
            cartItems.clear();
            txtCustomer.clear();
            txtContact.clear();
            updateTotal();
            updateHeldBillsMenu();
        });
    }

    private void updateHeldBillsMenu() {
        btnUnhold.getItems().clear();
        if (heldBills.isEmpty()) {
            btnUnhold.setVisible(false);
            return;
        }
        btnUnhold.setVisible(true);
        btnUnhold.setText("Held Bills (" + heldBills.size() + ")");
        
        for (String key : heldBills.keySet()) {
            MenuItem item = new MenuItem(key);
            item.setOnAction(e -> {
                if (!cartItems.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Current cart is not empty. Overwrite?");
                    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
                }
                cartItems.setAll(heldBills.get(key));
                heldBills.remove(key);
                updateTotal();
                updateHeldBillsMenu();
            });
            btnUnhold.getItems().add(item);
        }
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Cart is empty!").show();
            return;
        }
        if (txtCustomer.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Enter Customer Name!").show();
            return;
        }

        String customerName = txtCustomer.getText().trim();
        String contact = txtContact.getText().trim();
        String gst = txtGst.getText().trim();
        double total = cartItems.stream().mapToDouble(CartItem::getTotal).sum();

        logger.info("Initiating checkout for customer: {} (Total: {})", customerName, total);

        try {
            // THE NEW ATOMIC WAY: Send to InvoiceDAO which handles stock + invoice in ONE transaction
            new com.shopapp.database.InvoiceDAO().createInvoice(
                customerName, contact, gst, new ArrayList<>(cartItems), total
            );

            // Generate PDF
            String dirPath = "invoices";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "Invoice_" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(dir, fileName);
            
            PdfGenerator.generateInvoice(
                customerName, contact, gst,
                new ArrayList<>(cartItems), total,
                pdfFile.getAbsolutePath()
            );

            logger.info("Checkout successful. Invoice generated: {}", pdfFile.getName());

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Order Complete! Invoice saved.");
            alert.showAndWait();
            
            new Thread(() -> {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(pdfFile);
                    }
                } catch (Exception e) {
                    logger.error("Failed to open invoice PDF: {}", e.getMessage());
                }
            }).start();
            
            // UI Reset
            cartItems.clear();
            txtCustomer.clear();
            txtContact.clear();
            txtGst.clear();
            updateTotal();
            loadProducts();

        } catch (Exception e) {
            logger.error("Checkout failed for customer '{}': {}", customerName, e.getMessage(), e);
            new Alert(Alert.AlertType.ERROR, "Checkout Failed: " + e.getMessage()).show();
        }
    }

    private void handleBarcodeScan(String barcode) {
        if (barcode == null || barcode.isEmpty()) return;
        
        // Try to find the product in the currently available (possibly filtered) list
        for (Product p : availableProducts) {
            if (barcode.equalsIgnoreCase(p.getBarcode())) {
                addToCart(p);
                txtSearch.clear();
                return;
            }
        }
    }

    private void addShortcutsTooltip(Control control) {
        Tooltip tooltip = new Tooltip("Undo: Ctrl+Z | Redo: Ctrl+Y");
        Tooltip.install(control, tooltip);
    }
}
