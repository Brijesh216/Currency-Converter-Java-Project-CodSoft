package org.example;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

public class CurrencyConverter extends JFrame {

    private JComboBox<String> fromCurrencyDropdown;
    private JComboBox<String> toCurrencyDropdown;
    private JTextField amountField;
    private JLabel resultLabel;

    //  API Key
    private static final String API_KEY = /*" PASTE_YOUR_API_KEY "*/;

    public CurrencyConverter() {
        setTitle("Currency Converter");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load currencies dynamically from API
        Vector<String> currencies = fetchAllCurrencies();

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));

        panel.add(new JLabel("From Currency:"));
        fromCurrencyDropdown = new JComboBox<>(currencies);
        panel.add(fromCurrencyDropdown);

        panel.add(new JLabel("To Currency:"));
        toCurrencyDropdown = new JComboBox<>(currencies);
        panel.add(toCurrencyDropdown);

        panel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        panel.add(amountField);

        //  Swap Button
        JButton swapButton = new JButton("Swap Currencies 🔄");
        panel.add(swapButton);

        // Convert Button
        JButton convertButton = new JButton("Convert");
        panel.add(convertButton);

        resultLabel = new JLabel("Converted Amount: ");
        panel.add(resultLabel);

        add(panel);

        //  Swap Action
        swapButton.addActionListener(e -> {
            int fromIndex = fromCurrencyDropdown.getSelectedIndex();
            int toIndex = toCurrencyDropdown.getSelectedIndex();
            fromCurrencyDropdown.setSelectedIndex(toIndex);
            toCurrencyDropdown.setSelectedIndex(fromIndex);
        });

        // Convert Action
        convertButton.addActionListener(e -> {
            String fromCurrency = (String) fromCurrencyDropdown.getSelectedItem();
            String toCurrency = (String) toCurrencyDropdown.getSelectedItem();
            String amountText = amountField.getText();

            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered.");
                return;
            }

            convertCurrency(fromCurrency, toCurrency, amount);
        });
    }

    //  Fetch all currencies from API
    private Vector<String> fetchAllCurrencies() {
        Vector<String> currencies = new Vector<>();
        try {
            String urlStr = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";

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

            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                currencies.add(keys.next());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching currencies. Check API key or internet.");
            currencies.add("USD");
            currencies.add("EUR");
            currencies.add("INR");
        }
        return currencies;
    }

    //  Conversion Logic
    private void convertCurrency(String fromCurrency, String toCurrency, double amount) {
        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                String urlStr = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + fromCurrency;

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
                return rates.getDouble(toCurrency);
            }

            @Override
            protected void done() {
                try {
                    double rate = get();
                    double convertedAmount = amount * rate;
                    resultLabel.setText("Converted Amount: " + String.format("%.2f", convertedAmount) + " " + toCurrency);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    resultLabel.setText("Error in conversion.");
                }
            }
        };

        worker.execute();
    }

    //  Main Method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CurrencyConverter converter = new CurrencyConverter();
            converter.setVisible(true);
        });
    }
}
