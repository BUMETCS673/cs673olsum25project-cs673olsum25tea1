/* eslint-disable no-undef */
describe("Home Page E2E Test", () => {
  it("should allow a user to register successfully", () => {
    // Intercept the registration request to capture the token
    cy.intercept("POST", "/v1/register", (req) => {
      req.continue((res) => {
        const token = res.body?.data?.token;
        Cypress.env("token", token);
      });
    }).as("registrationToken");

    cy.visit("/register");

    cy.findByLabelText(/Username/i).type("testuser2");
    cy.findByLabelText(/Email/i).type("testuser2@bu.edu");
    cy.get('input[id="password"]').should("be.visible").type("Password123!");
    cy.get('input[id="confirmpassword"]').should("be.visible").type("Password123!");

    cy.findByRole("button", { name: /Register/i }).click();

    // Wait for the backend to respond
    cy.wait("@registrationToken").then(() => {
      const token = Cypress.env("token");
      expect(token).to.exist;

      // Wait for us to be redirected to the confirmation page
      cy.url().should("include", "/register/confirmation");

      cy.findByLabelText(/Registration Code/i).type(token);
      cy.findByRole("button", { name: /Confirm/i }).click();

      cy.contains("Confirmation successful").should("be.visible");

      // Check that user is redirected or sees success
      cy.url().should("include", "/login");
    });
  });

  it("should allow a user to log in successfully", () => {
    cy.visit("/login");
    cy.findByLabelText(/username/i).type("testuser2");
    cy.findByLabelText(/password/i).type("Password123!");
    cy.findByRole("button", { name: /Login/i }).click();

    cy.url().should("include", "/home");
    cy.contains(/ActivityHub/i).should("be.visible");
    cy.contains("Rock Climbing")
      .closest("div")
      .within(() => {
        cy.contains("button", "Join Activity").click();
      });
    cy.contains(/Participated Activities/i).click();
    cy.contains(/Rock Climbing/i).should("be.visible");
    cy.contains("Rock Climbing")
      .closest("div")
      .within(() => {
        cy.contains("button", "Leave Activity").click();
      });
    cy.contains(/Rock Climbing/i).should("not.exist");
  });
});
