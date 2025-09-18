package org.example;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class CurrencyConverterAll extends JFrame {

    private static final String API_KEY = /*" PASTE_YOUR_API_KEY "*/;
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private JComboBox<String> baseCurrencyDropdown;
    private JComboBox<String> targetCurrencyDropdown;
    private JTextField amountField;
    private JLabel resultLabel;

    public CurrencyConverterAll() {
        setTitle("Currency Converter");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        JLabel baseLabel = new JLabel("Base Currency:");
        baseCurrencyDropdown = new JComboBox<>();

        JLabel targetLabel = new JLabel("Target Currency:");
        targetCurrencyDropdown = new JComboBox<>();

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JButton swapButton = new JButton("Swap ↔");
        JButton convertButton = new JButton("Convert");
        resultLabel = new JLabel("Result: ");

        add(baseLabel);
        add(baseCurrencyDropdown);
        add(targetLabel);
        add(targetCurrencyDropdown);
        add(amountLabel);
        add(amountField);
        add(swapButton);
        add(convertButton);
        add(new JLabel()); // Empty cell
        add(resultLabel);

        // Fetch currency list initially
        fetchCurrencies();

        //  Swap button logic
        swapButton.addActionListener(e -> {
            String base = (String) baseCurrencyDropdown.getSelectedItem();
            baseCurrencyDropdown.setSelectedItem(targetCurrencyDropdown.getSelectedItem());
            targetCurrencyDropdown.setSelectedItem(base);
            convertCurrency(); // auto convert after swap
        });

        // Convert button logic
        convertButton.addActionListener(e -> convertCurrency());

        // 📡 Live update when typing in amount field
        amountField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                convertCurrency();
            }
        });

        // 📡 Live update when changing dropdowns
        baseCurrencyDropdown.addActionListener(e -> convertCurrency());
        targetCurrencyDropdown.addActionListener(e -> convertCurrency());
    }

    private void fetchCurrencies() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String urlStr = API_URL + "USD"; // Using USD as base for fetching all currencies
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject obj = new JSONObject(response.toString());
                    JSONObject rates = obj.getJSONObject("conversion_rates");

                    SwingUtilities.invokeLater(() -> {
                        for (Iterator<String> it = rates.keys(); it.hasNext(); ) {
                            String currency = it.next();
                            baseCurrencyDropdown.addItem(currency);
                            targetCurrencyDropdown.addItem(currency);
                        }
                        baseCurrencyDropdown.setSelectedItem("USD");
                        targetCurrencyDropdown.setSelectedItem("INR");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error fetching currencies.");
                }
                return null;
            }
        };
        worker.execute();
    }

    private void convertCurrency() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    String baseCurrency = (String) baseCurrencyDropdown.getSelectedItem();
                    String targetCurrency = (String) targetCurrencyDropdown.getSelectedItem();
                    String amountText = amountField.getText();

                    if (baseCurrency == null || targetCurrency == null || amountText.isEmpty()) {
                        return null;
                    }

                    double amount = Double.parseDouble(amountText);

                    String urlStr = API_URL + baseCurrency;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject obj = new JSONObject(response.toString());
                    JSONObject rates = obj.getJSONObject("conversion_rates");

                    double rate = rates.getDouble(targetCurrency);
                    double result = amount * rate;

                    SwingUtilities.invokeLater(() -> resultLabel.setText("Result: " + result + " " + targetCurrency));
                } catch (Exception ex) {
                    // Ignore empty input errors
                }
                return null;
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverterAll().setVisible(true));
    }
}
