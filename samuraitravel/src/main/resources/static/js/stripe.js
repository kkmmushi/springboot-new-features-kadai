const stripe = Stripe('pk_test_51Q1MHv05GRo9SttlNb86OnSgU9gAD6bftgj2P0ZOvqQOtkZzgXlotDTzhWhztE2zvQCrOhq7o9HOuj7hEIvgycVq00ECNxhmIa');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
  stripe.redirectToCheckout({
    sessionId: sessionId
  })
});