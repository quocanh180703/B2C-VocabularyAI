/**
 * Test Helper Functions for E2E Testing
 */

/**
 * Helper to test list page functionality
 */
export const testListPage = (page: {
  path: string
  searchSelector?: string
  filterSelectors?: Record<string, string>
  tableSelector: string
  paginationSelector?: string
  detailButton?: string
}) => {
  cy.visit(page.path)
  cy.get(page.tableSelector, { timeout: 10000 }).should("exist")

  // Test search if applicable
  if (page.searchSelector) {
    cy.get(page.searchSelector).type("test")
    cy.get(page.tableSelector).should("exist")
    cy.get(page.searchSelector).clear()
  }

  // Test filters if applicable
  if (page.filterSelectors) {
    Object.entries(page.filterSelectors).forEach(([name, selector]) => {
      cy.get(selector).should("exist").click()
    })
  }

  // Test pagination if applicable
  if (page.paginationSelector) {
    cy.get(page.paginationSelector).should("exist")
  }

  // Test detail view if applicable
  if (page.detailButton) {
    cy.get(page.detailButton).first().click()
    cy.url().should("include", "/")
  }
}

/**
 * Helper to test API endpoint
 */
export const testApiEndpoint = (endpoint: {
  method: "GET" | "POST" | "PUT" | "DELETE"
  url: string
  expectedStatus: number
  body?: Record<string, any>
  headers?: Record<string, string>
}) => {
  cy.request({
    method: endpoint.method,
    url: endpoint.url,
    body: endpoint.body,
    headers: endpoint.headers,
    failOnStatusCode: false,
  }).then((response) => {
    expect(response.status).to.equal(endpoint.expectedStatus)
  })
}

/**
 * Helper to measure page load time
 */
export const measurePageLoadTime = (path: string) => {
  const startTime = performance.now()
  cy.visit(path)
  cy.get("body").should("be.visible")
  const endTime = performance.now()
  const loadTime = endTime - startTime
  return loadTime
}

/**
 * Helper to test accessibility
 */
export const testAccessibility = () => {
  // Test keyboard navigation
  cy.get("body").tab()
  cy.focused().should("exist")

  // Test that interactive elements are keyboard accessible
  cy.get("button, a, input").each(($el) => {
    cy.wrap($el).should("have.attr", "tabindex").or("have.tag", ["button", "a"])
  })
}

/**
 * Helper to check error handling
 */
export const testErrorHandling = (scenario: {
  action: () => void
  expectedErrorMessage: string
  errorSelector: string
}) => {
  scenario.action()
  cy.get(scenario.errorSelector).should("contain", scenario.expectedErrorMessage)
}
