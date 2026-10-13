// Cypress support file for E2E tests

// Add custom commands
Cypress.Commands.add("login", (email: string, password: string) => {
  cy.visit("/login")
  cy.get('input[type="email"]').type(email)
  cy.get('input[type="password"]').type(password)
  cy.get('button[type="submit"]').click()
  cy.url().should("include", "/admin/dashboard")
})

Cypress.Commands.add("navigateTo", (path: string) => {
  cy.visit(path)
  cy.get("body").should("be.visible")
})

Cypress.Commands.add("waitForData", (selector: string) => {
  cy.get(selector, { timeout: 10000 }).should("exist").and("be.visible")
})

// Override types
declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>
      navigateTo(path: string): Chainable<void>
      waitForData(selector: string): Chainable<void>
    }
  }
}

export {}
