package com.shopapp.ui;

import com.shopapp.database.ProductDAO;
import com.shopapp.models.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class InventoryView extends HBox {
    private TableView<Product> table;
    private ObservableList<Product> productList;
    private FilteredList<Product> filteredData;
    private ProductDAO productDAO;

    // Form Fields
    private TextField txtName = new TextField();
    private TextField txtBarcode = new TextField();
    private TextField txtHsn = new TextField();
    private TextField txtPrice = new TextField();
    private TextField txtStock = new TextField();
    private TextField txtMinStock = new TextField();
    private TextArea txtDesc = new TextArea();
    
    private Button btnSave;
    private Button btnDelete;
    private Product selectedProduct = null;
    private TextField searchField;
    private ProgressIndicator loadingIndicator;
    private final boolean showLowStockOnly;

    public InventoryView(boolean showLowStockOnly) {
        this.showLowStockOnly = showLowStockOnly;
        productDAO = new ProductDAO();
        setSpacing(25);
        setPadding(new Insets(10));
        
        // --- Left Side: Product List ---
        VBox leftSection = new VBox(15);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        
        // Header & Search
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        String titleText = showLowStockOnly ? "Low Stock Alerts" : "Product Inventory";
        Label lblTitle = new Label(titleText);
        lblTitle.getStyleClass().add("page-title");
        if (showLowStockOnly) lblTitle.setStyle("-fx-text-fill: #ef4444;"); // Red title for alert mode
        
        lblTitle.setPadding(Insets.EMPTY); 
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        searchField = new TextField();
        searchField.setPromptText("🔍 Search products...");
        searchField.setPrefWidth(250);
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);

        header.getChildren().addAll(lblTitle, spacer, loadingIndicator, searchField);
        
        // Auto-focus search on load
        Platform.runLater(() -> searchField.requestFocus());
        
        table = new TableView<>();
        setupTable();
        loadData();
        
        // Setup Search Listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredData != null) {
                filteredData.setPredicate(product -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (product.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                    if (product.getBarcode().toLowerCase().contains(lowerCaseFilter)) return true;
                    return false;
                });
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        leftSection.getChildren().addAll(header, table);

        // --- Right Side: Add/Edit Form ---
        VBox formContainer = new VBox(15);
        formContainer.setPrefWidth(380);
        formContainer.getStyleClass().add("card");
        
        Label lblForm = new Label("Add / Edit Product");
        lblForm.getStyleClass().add("card-title");

        btnSave = new Button("Save Product");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> saveProduct());
        
        Button btnClear = new Button("Clear Form");
        btnClear.getStyleClass().add("nav-button"); 
        btnClear.getStyleClass().add("clear-button");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> clearForm());

        btnDelete = new Button("Delete Selected");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setDisable(true);
        btnDelete.setOnAction(e -> deleteProduct());
        
        txtDesc.setPrefRowCount(3);

        // Numeric validation
        addNumericListener(txtPrice);
        addNumericListener(txtStock);
        addNumericListener(txtMinStock);

        // Add Undo/Redo tooltips to form fields
        addShortcutsTooltip(txtName);
        addShortcutsTooltip(txtBarcode);
        addShortcutsTooltip(txtHsn);
        addShortcutsTooltip(txtPrice);
        addShortcutsTooltip(txtStock);
        addShortcutsTooltip(txtMinStock);
        addShortcutsTooltip(txtDesc);

        formContainer.getChildren().addAll(
            lblForm,
            createFormGroup("Product Name", txtName),
            createFormGroup("Barcode", txtBarcode),
            createFormGroup("HSN Code", txtHsn),
            createFormGroup("Price ($)", txtPrice),
            createFormGroup("Current Stock", txtStock),
            createFormGroup("Min Stock Alert", txtMinStock),
            createFormGroup("Description", txtDesc),
            new Region() {{ setMinHeight(10); }},
            btnSave, btnClear, btnDelete
        );
        VBox.setVgrow(formContainer, Priority.ALWAYS); // Stretch height

        getChildren().addAll(leftSection, formContainer);
        
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

        // Form shortcuts
        formContainer.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !txtDesc.isFocused()) {
                saveProduct();
            } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                clearForm();
                searchField.requestFocus();
            }
        });
    }
    
    private VBox createFormGroup(String labelText, Control input) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.getStyleClass().add("label");
        box.getChildren().addAll(label, input);
        return box;
    }

    private void addNumericListener(TextField tf) {
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                tf.setText(oldValue);
            }
        });
    }

    private void addShortcutsTooltip(Control control) {
        Tooltip tooltip = new Tooltip("Undo: Ctrl+Z | Redo: Ctrl+Y");
        Tooltip.install(control, tooltip);
    }

    private void setupTable() {
        TableColumn<Product, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Product, String> colBarcode = new TableColumn<>("Barcode");
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        
        TableColumn<Product, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        
        TableColumn<Product, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        
        // Stock Alert Highlighting
        colStock.setCellFactory(column -> new TableCell<Product, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    Product p = getTableView().getItems().get(getIndex());
                    if (p.getCurrentStock() <= p.getMinStockAlert()) {
                        setTextFill(javafx.scene.paint.Color.rgb(252, 165, 165)); // Red 300
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(null); // Reset to default (CSS handled)
                        setStyle("");
                    }
                }
            }
        });

        // Column Widths (Proportional)
        colName.prefWidthProperty().bind(table.widthProperty().multiply(0.40));
        colBarcode.prefWidthProperty().bind(table.widthProperty().multiply(0.20));
        colPrice.prefWidthProperty().bind(table.widthProperty().multiply(0.20));
        colStock.prefWidthProperty().bind(table.widthProperty().multiply(0.19));
        
        table.getColumns().addAll(colName, colBarcode, colPrice, colStock);
        
        // Alignment
        colName.setStyle("-fx-alignment: CENTER-LEFT;");
        colBarcode.setStyle("-fx-alignment: CENTER;");
        colPrice.setStyle("-fx-alignment: CENTER-RIGHT;");
        colStock.setStyle("-fx-alignment: CENTER;");
    }

    private void loadData() {
        loadingIndicator.setVisible(true);
        Task<List<Product>> loadTask = new Task<>() {
            @Override
            protected List<Product> call() throws Exception {
                // Simulate network latency if needed, but not here for local DB
                return productDAO.getAllProducts();
            }
        };

        loadTask.setOnSucceeded(e -> {
            loadingIndicator.setVisible(false);
            try {
                productList = FXCollections.observableArrayList(loadTask.getValue());
                
                // Initial Filter
                filteredData = new FilteredList<>(productList, p -> {
                    if (showLowStockOnly) {
                        return p.getCurrentStock() <= p.getMinStockAlert();
                    }
                    return true;
                });
                
                table.setItems(filteredData);
            } catch (Exception ex) { ex.printStackTrace(); }
            
            // Re-apply filter if search box has text (Combine logic)
            applyFilters();
        });

        loadTask.setOnFailed(e -> {
            loadingIndicator.setVisible(false);
            new Alert(Alert.AlertType.ERROR, "Failed to load data.").show();
        });

        new Thread(loadTask).start();
        
        // Setup Search Listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }
    
    private void applyFilters() {
        if (filteredData == null) return;
        
        filteredData.setPredicate(product -> {
            // 1. Check Low Stock Filter
            if (showLowStockOnly && product.getCurrentStock() > product.getMinStockAlert()) {
                return false; 
            }
            
            // 2. Check Search Text
            String newValue = searchField.getText();
            if (newValue == null || newValue.isEmpty()) return true;
            
            String lowerCaseFilter = newValue.toLowerCase();
            return product.getName().toLowerCase().contains(lowerCaseFilter) || 
                   product.getBarcode().toLowerCase().contains(lowerCaseFilter);
        });
    }

    private void saveProduct() {
        try {
            Product p = selectedProduct != null ? selectedProduct : new Product();
            p.setName(txtName.getText());
            p.setBarcode(txtBarcode.getText());
            p.setHsnCode(txtHsn.getText());
            p.setPricePerUnit(Double.parseDouble(txtPrice.getText()));
            p.setCurrentStock(Integer.parseInt(txtStock.getText()));
            p.setMinStockAlert(Integer.parseInt(txtMinStock.getText()));
            p.setDescription(txtDesc.getText());

            if (selectedProduct == null) {
                productDAO.addProduct(p);
            } else {
                productDAO.updateProduct(p);
            }
            clearForm();
            loadData();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error saving product: " + e.getMessage()).show();
        }
    }

    private void deleteProduct() {
        if (selectedProduct != null) {
            try {
                productDAO.deleteProduct(selectedProduct.getId());
                clearForm();
                loadData();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Error deleting product: " + e.getMessage()).show();
            }
        }
    }

    private void populateForm(Product p) {
        selectedProduct = p;
        txtName.setText(p.getName());
        txtBarcode.setText(p.getBarcode());
        txtHsn.setText(p.getHsnCode());
        txtPrice.setText(String.valueOf(p.getPricePerUnit()));
        txtStock.setText(String.valueOf(p.getCurrentStock()));
        txtMinStock.setText(String.valueOf(p.getMinStockAlert()));
        txtDesc.setText(p.getDescription());
        btnSave.setText("Update Product");
        btnDelete.setDisable(false);
    }

    private void clearForm() {
        selectedProduct = null;
        txtName.clear();
        txtBarcode.clear();
        txtHsn.clear();
        txtPrice.clear();
        txtStock.clear();
        txtMinStock.clear();
        txtDesc.clear();
        btnSave.setText("Save Product");
        btnDelete.setDisable(true);
        table.getSelectionModel().clearSelection();
    }
}
