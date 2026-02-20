package com.shopapp.ui;

import com.shopapp.database.SupplierDAO;
import com.shopapp.models.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class SuppliersView extends HBox {
    private TableView<Supplier> table;
    private ObservableList<Supplier> supplierList;
    private SupplierDAO supplierDAO;

    // Form
    private TextField txtName = new TextField();
    private TextField txtContact = new TextField();
    private TextField txtPhone = new TextField();
    private TextField txtEmail = new TextField();
    private TextArea txtAddress = new TextArea();
    
    private Button btnSave;
    private Button btnDelete;
    private Supplier selectedSupplier = null;

    public SuppliersView() {
        supplierDAO = new SupplierDAO();
        setSpacing(20);
        setPadding(new Insets(20));
        
        // --- Table Section ---
        VBox tableContainer = new VBox(10);
        HBox.setHgrow(tableContainer, Priority.ALWAYS);
        
        Label lblTitle = new Label("Supplier Management");
        lblTitle.getStyleClass().add("page-title");
        
        table = new TableView<>();
        setupTable();
        loadData();
        
        VBox.setVgrow(table, Priority.ALWAYS);
        tableContainer.getChildren().addAll(lblTitle, table);

        // --- Form Section ---
        VBox formContainer = new VBox(15);
        formContainer.setPrefWidth(350);
        formContainer.getStyleClass().add("card");
        
        Label lblForm = new Label("Add / Edit Supplier");
        lblForm.getStyleClass().add("card-title");

        btnSave = new Button("Save Supplier");
        btnSave.getStyleClass().add("button-primary");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> saveSupplier());
        
        Button btnClear = new Button("Clear Form");
        btnClear.getStyleClass().add("nav-button");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> clearForm());

        btnDelete = new Button("Delete Selected");
        btnDelete.getStyleClass().add("button-danger");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setDisable(true);
        btnDelete.setOnAction(e -> deleteSupplier());
        
        txtAddress.setPrefRowCount(3);

        VBox.setVgrow(formContainer, Priority.ALWAYS);
        formContainer.getChildren().addAll(
            lblForm,
            new Label("Company Name"), txtName,
            new Label("Contact Person"), txtContact,
            new Label("Phone"), txtPhone,
            new Label("Email"), txtEmail,
            new Label("Address"), txtAddress,
            btnSave, btnClear, btnDelete
        );

        getChildren().addAll(tableContainer, formContainer);
        
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) populateForm(val);
        });
    }

    private void setupTable() {
        TableColumn<Supplier, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Supplier, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        TableColumn<Supplier, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        table.getColumns().addAll(colName, colPhone, colEmail);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadData() {
        Task<List<Supplier>> task = new Task<>() {
            @Override
            protected List<Supplier> call() throws Exception {
                return supplierDAO.getAllSuppliers();
            }
        };
        task.setOnSucceeded(e -> {
            supplierList = FXCollections.observableArrayList(task.getValue());
            table.setItems(supplierList);
        });
        new Thread(task).start();
    }

    private void saveSupplier() {
        if (txtName.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Name required").show();
            return;
        }

        try {
            Supplier s = new Supplier(
                selectedSupplier == null ? 0 : selectedSupplier.getId(),
                txtName.getText(),
                txtContact.getText(),
                txtPhone.getText(),
                txtEmail.getText(),
                txtAddress.getText()
            );

            if (selectedSupplier == null) {
                supplierDAO.addSupplier(s);
            } else {
                supplierDAO.updateSupplier(s);
            }
            clearForm();
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void deleteSupplier() {
        if (selectedSupplier != null) {
            try {
                supplierDAO.deleteSupplier(selectedSupplier.getId());
                clearForm();
                loadData();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void populateForm(Supplier s) {
        selectedSupplier = s;
        txtName.setText(s.getName());
        txtContact.setText(s.getContactPerson());
        txtPhone.setText(s.getPhone());
        txtEmail.setText(s.getEmail());
        txtAddress.setText(s.getAddress());
        btnSave.setText("Update Supplier");
        btnDelete.setDisable(false);
    }

    private void clearForm() {
        selectedSupplier = null;
        txtName.clear();
        txtContact.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtAddress.clear();
        btnSave.setText("Save Supplier");
        btnDelete.setDisable(true);
        table.getSelectionModel().clearSelection();
    }
}
