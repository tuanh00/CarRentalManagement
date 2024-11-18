require('dotenv').config(); // Load environment variables from .env file

const express = require('express');
const Stripe = require('stripe');
const app = express();
const stripe = Stripe(process.env.STRIPE_SECRET_KEY); // Replace with your actual Stripe secret key

app.use(express.json());

app.post('/payment-sheet', async (req, res) => {
    try {
        const { email, name, totalAmount } = req.body;

        // Step 1: Create a Customer
        const customer = await stripe.customers.create({
            email: email,
            name: name,
        });
        // Step 2: Create an Ephemeral Key for the Customer
        const ephemeralKey = await stripe.ephemeralKeys.create(
            { customer: customer.id },
            { apiVersion: '2022-11-15' }
        );

        // Step 3: Create a PaymentIntent with the Customer ID
        const paymentIntent = await stripe.paymentIntents.create({
            amount: totalAmount,
            currency: 'cad',
            customer: customer.id,
            automatic_payment_methods: { enabled: true },
        });

        res.send({
            paymentIntent: paymentIntent.client_secret,
            ephemeralKey: ephemeralKey.secret,
            customer: customer.id,
            publishableKey: process.env.STRIPE_PUBLISHABLE_KEY, // Replace with your actual publishable key
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(4242, () => console.log('Server running on port 4242'));