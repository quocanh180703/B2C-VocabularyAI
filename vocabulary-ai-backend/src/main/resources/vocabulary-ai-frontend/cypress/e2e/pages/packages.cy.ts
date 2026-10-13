describe("Packages Page E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/packages")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load packages page successfully", () => {
      cy.url().should("include", "/admin/packages")
      cy.contains("Subscription Packages").should("be.visible")
    })

    it("should display page description", () => {
      cy.contains("Manage subscription packages and pricing").should("be.visible")
    })

    it("should display packages table", () => {
      cy.get("table").should("be.visible")
      cy.get("th").should("contain", "Name")
      cy.get("th").should("contain", "Code")
      cy.get("th").should("contain", "Price")
      cy.get("th").should("contain", "Status")
    })

    it("should display search and filter controls", () => {
      cy.get("input[placeholder*='Search']").should("be.visible")
      cy.get("select").should("exist")
    })

    it("should load package data", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Search Functionality", () => {
    it("should search packages by name", () => {
      cy.get("input[placeholder*='Search']").type("premium")
      cy.get("tbody tr").should("exist")
    })

    it("should search packages by code", () => {
      cy.get("input[placeholder*='Search']").type("PKG")
      cy.get("tbody tr").should("exist")
    })

    it("should clear search and show all results", () => {
      cy.get("input[placeholder*='Search']").type("test")
      cy.get("input[placeholder*='Search']").clear()
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })

    it("should show empty state when search has no results", () => {
      cy.get("input[placeholder*='Search']").type("xyznonexistent123")
      cy.get("tbody tr").should("have.length", 0)
      cy.contains("No packages found").should("be.visible")
    })
  })

  describe("Filtering by Status", () => {
    it("should filter by ACTIVE status", () => {
      cy.get("select").select("ACTIVE")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by INACTIVE status", () => {
      cy.get("select").select("INACTIVE")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by ARCHIVED status", () => {
      cy.get("select").select("ARCHIVED")
      cy.get("tbody tr").should("exist")
    })

    it("should reset status filter to All", () => {
      cy.get("select").select("ACTIVE")
      cy.get("select").select("All")
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Combined Search and Filter", () => {
    it("should combine search with status filter", () => {
      cy.get("input[placeholder*='Search']").type("premium")
      cy.get("select").select("ACTIVE")
      cy.get("tbody tr").should("exist")
    })

    it("should reset all search and filters", () => {
      cy.get("input[placeholder*='Search']").type("test")
      cy.get("select").select("ACTIVE")
      
      cy.get("input[placeholder*='Search']").clear()
      cy.get("select").select("All")
      
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Table Display and Data", () => {
    it("should display all table columns with data", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").should("have.length.greaterThan", 6)
      })
    })

    it("should display package name", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).should("not.be.empty")
      })
    })

    it("should display package code in monospace font", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(1).should("have.class", "font-mono")
      })
    })

    it("should display price in proper format", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(2).should("match", /\$\d+\.\d{2}/)
      })
    })

    it("should display duration in days", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(3).should("contain", "days")
      })
    })

    it("should display status badge", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains(/ACTIVE|INACTIVE|ARCHIVED/).should("be.visible")
      })
    })

    it("should display total purchases", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(5).should("match", /\d+/)
      })
    })

    it("should display revenue in proper format", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(6).should("match", /\$\d+\.\d{2}/)
      })
    })

    it("should display creation date", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(7).should("match", /\d{1,2}\/\d{1,2}\/\d{4}/)
      })
    })

    it("should have view button for each row", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").should("exist")
      })
    })
  })

  describe("Status Badges", () => {
    it("should display active status badge with success color", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains("ACTIVE").parent().should("have.attr", "class")
      })
    })

    it("should display inactive status badge with warning color", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains("INACTIVE").parent().should("have.attr", "class")
      })
    })
  })

  describe("Pagination", () => {
    it("should display pagination controls", () => {
      cy.get("[data-testid='pagination']").should("exist")
    })

    it("should navigate to next page", () => {
      cy.get("tbody tr").first().then(($row) => {
        const firstPageFirstRow = $row.text()
        cy.get("button").contains("Next").click({ force: true })
        cy.get("tbody tr").first().then(($nextRow) => {
          const secondPageFirstRow = $nextRow.text()
          expect(firstPageFirstRow).not.to.equal(secondPageFirstRow)
        })
      })
    })

    it("should navigate to previous page", () => {
      cy.get("button").contains("Next").click({ force: true })
      cy.get("tbody tr").should("exist")
      cy.get("button").contains("Previous").click({ force: true })
      cy.get("tbody tr").should("exist")
    })
  })

  describe("Detail View", () => {
    it("should navigate to detail page when clicking view button", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.url().should("include", "/admin/packages/")
      cy.contains("Package Information").should("be.visible")
    })

    it("should display package details on detail page", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("Description").should("be.visible")
      cy.contains("Pricing").should("be.visible")
    })

    it("should display package information section", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("Package Code").should("be.visible")
      cy.contains("Duration").should("be.visible")
      cy.contains("Total Purchases").should("be.visible")
      cy.contains("Total Revenue").should("be.visible")
      cy.contains("Active Subscriptions").should("be.visible")
    })

    it("should display pricing information", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("for").should("be.visible")
      cy.contains("days").should("be.visible")
    })

    it("should have back button on detail page", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.get("button").contains("Back to packages").click()
      cy.url().should("include", "/admin/packages")
    })

    it("should show 404 when package not found", () => {
      cy.visit("/admin/packages/99999")
      cy.contains("Package not found").should("be.visible")
    })
  })

  describe("Responsive Design", () => {
    it("should be responsive on mobile viewport", () => {
      cy.viewport("iphone-12")
      cy.get("table").should("be.visible")
      cy.get("input[placeholder*='Search']").should("be.visible")
    })

    it("should scroll table horizontally on mobile", () => {
      cy.viewport("iphone-12")
      cy.get("table").should("have.css", "overflow-x")
    })

    it("should be responsive on tablet viewport", () => {
      cy.viewport("ipad-2")
      cy.get("table").should("be.visible")
      cy.get("select").should("be.visible")
    })

    it("should be responsive on desktop viewport", () => {
      cy.viewport(1920, 1080)
      cy.get("table").should("be.visible")
      cy.get("[data-testid='pagination']").should("be.visible")
    })
  })

  describe("Performance", () => {
    it("should load page within acceptable time", () => {
      const startTime = performance.now()
      cy.visit("/admin/packages")
      cy.get("table").should("be.visible")
      const endTime = performance.now()
      const loadTime = endTime - startTime
      expect(loadTime).to.be.lessThan(3000) // 3 seconds
    })

    it("should render table rows quickly", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
      cy.get("tbody tr").should("be.visible")
    })

    it("should apply filters without significant delay", () => {
      const startTime = performance.now()
      cy.get("select").select("ACTIVE")
      cy.get("tbody tr").should("exist")
      const endTime = performance.now()
      const filterTime = endTime - startTime
      expect(filterTime).to.be.lessThan(2000) // 2 seconds
    })
  })

  describe("Accessibility", () => {
    it("should have proper heading structure", () => {
      cy.get("h1").should("exist")
    })

    it("should have accessible search input", () => {
      cy.get("input[placeholder*='Search']")
        .should("have.attr", "aria-label")
        .or("have.attr", "placeholder")
    })

    it("should have accessible select dropdown", () => {
      cy.get("select").should("have.attr", "aria-label").or("have.attr", "id")
    })

    it("should have accessible table headers", () => {
      cy.get("th").each(($th) => {
        expect($th).to.have.text()
      })
    })

    it("should have accessible buttons", () => {
      cy.get("button").each(($btn) => {
        expect($btn).to.have.text().or("have.attr", "aria-label")
      })
    })

    it("should support keyboard navigation", () => {
      cy.get("input[placeholder*='Search']").focus()
      cy.focused().should("have.attr", "placeholder")
      
      cy.get("select").focus()
      cy.focused().should("be.a", "select")
    })
  })

  describe("Error Handling", () => {
    it("should handle network errors gracefully", () => {
      cy.intercept("GET", "**/admin/packages", { statusCode: 500 })
      cy.visit("/admin/packages")
    })

    it("should handle empty data gracefully", () => {
      cy.intercept("GET", "**/admin/packages", {
        statusCode: 200,
        body: { content: [], totalPages: 1 }
      })
      cy.visit("/admin/packages")
      cy.get("tbody tr").should("have.length", 0)
    })

    it("should show notification on search error", () => {
      cy.intercept("GET", "**/admin/packages*", { statusCode: 500 }).as("packageSearch")
      cy.get("input[placeholder*='Search']").type("test")
      cy.wait("@packageSearch")
    })
  })

  describe("Data Consistency", () => {
    it("should maintain filters during pagination", () => {
      cy.get("select").select("ACTIVE")
      cy.get("button").contains("Next").click({ force: true })
      cy.get("select").should("have.value", "ACTIVE")
    })

    it("should maintain search query during filter change", () => {
      const searchTerm = "premium"
      cy.get("input[placeholder*='Search']").type(searchTerm)
      cy.get("select").select("ACTIVE")
      cy.get("input[placeholder*='Search']").should("have.value", searchTerm)
    })

    it("should display consistent data in list and detail view", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).then(($name) => {
          const packageName = $name.text()
          cy.get("button").click({ force: true })
          cy.get("h1").should("contain", packageName)
        })
      })
    })

    it("should maintain sort order during pagination", () => {
      cy.get("tbody tr").first().then(($row) => {
        const firstPageFirstPackage = $row.text()
        cy.get("button").contains("Next").click({ force: true })
        cy.get("tbody tr").first().then(($nextRow) => {
          const secondPageFirstPackage = $nextRow.text()
          expect(firstPageFirstPackage).not.to.equal(secondPageFirstPackage)
        })
      })
    })
  })

  describe("Pricing Display", () => {
    it("should display prices with dollar sign", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(2).should("match", /^\$/)
      })
    })

    it("should display prices with two decimal places", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(2).should("match", /\.\d{2}$/)
      })
    })

    it("should display revenue amounts correctly", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(6).should("match", /^\$\d+\.\d{2}$/)
      })
    })
  })
})
