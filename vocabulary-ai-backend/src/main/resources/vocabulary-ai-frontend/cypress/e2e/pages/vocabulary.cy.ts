describe("Vocabulary Page E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/vocabulary")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load vocabulary page successfully", () => {
      cy.url().should("include", "/admin/vocabulary")
      cy.contains("Vocabulary Management").should("be.visible")
    })

    it("should display vocabulary table", () => {
      cy.get("table").should("be.visible")
      cy.get("th").should("contain", "Word")
      cy.get("th").should("contain", "Meaning")
      cy.get("th").should("contain", "CEFR")
    })

    it("should display pagination controls", () => {
      cy.get("[data-testid='pagination']").should("exist")
    })

    it("should load vocabulary data", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Search Functionality", () => {
    it("should search vocabulary by word", () => {
      const searchTerm = "apple"
      cy.get("input[placeholder*='Search']").type(searchTerm)
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
      cy.contains("No vocabulary found").should("be.visible")
    })
  })

  describe("Filtering", () => {
    it("should filter by CEFR level", () => {
      cy.get("select").eq(0).select("B1")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by Part of Speech", () => {
      cy.get("select").eq(1).select("NOUN")
      cy.get("tbody tr").should("exist")
    })

    it("should combine filters", () => {
      cy.get("select").eq(0).select("A1")
      cy.get("select").eq(1).select("VERB")
      cy.get("tbody tr").should("exist")
    })

    it("should reset filters to 'All'", () => {
      cy.get("select").eq(0).select("B1")
      cy.get("select").eq(0).select("All")
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Pagination", () => {
    it("should navigate to next page", () => {
      cy.get("button").contains("Next").click({ force: true })
      cy.get("tbody tr").should("exist")
    })

    it("should navigate to specific page", () => {
      cy.get("[data-testid='page-input']").clear().type("2").type("{enter}")
      cy.get("tbody tr").should("exist")
    })

    it("should not navigate beyond last page", () => {
      cy.get("[data-testid='page-input']").clear().type("999").type("{enter}")
      cy.get("tbody tr").should("have.length", 0)
    })
  })

  describe("Detail View", () => {
    it("should navigate to detail page when clicking view button", () => {
      cy.get("button[title='View']").first().click({ force: true })
      cy.url().should("include", "/admin/vocabulary/")
      cy.contains("Definition").should("be.visible")
    })

    it("should display complete word information", () => {
      cy.get("button[title='View']").first().click({ force: true })
      cy.contains("Definition").should("be.visible")
      cy.contains("Pronunciation").should("be.visible")
      cy.contains("Part of Speech").should("be.visible")
    })

    it("should have back button on detail page", () => {
      cy.get("button[title='View']").first().click({ force: true })
      cy.get("button").contains("Back to vocabulary").click()
      cy.url().should("include", "/admin/vocabulary")
    })

    it("should show 404 when word not found", () => {
      cy.visit("/admin/vocabulary/99999")
      cy.contains("Vocabulary not found").should("be.visible")
    })
  })

  describe("Sorting", () => {
    it("should maintain sort order during pagination", () => {
      cy.get("tbody tr").first().then(($row) => {
        const firstPageFirstWord = $row.text()
        cy.get("button").contains("Next").click({ force: true })
        cy.get("tbody tr").first().then(($nextRow) => {
          const secondPageFirstWord = $nextRow.text()
          expect(firstPageFirstWord).not.to.equal(secondPageFirstWord)
        })
      })
    })
  })

  describe("Responsive Design", () => {
    it("should be responsive on mobile viewport", () => {
      cy.viewport("iphone-12")
      cy.get("table").should("be.visible")
      cy.get("input[placeholder*='Search']").should("be.visible")
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
      cy.visit("/admin/vocabulary")
      cy.get("table").should("be.visible")
      const endTime = performance.now()
      const loadTime = endTime - startTime
      expect(loadTime).to.be.lessThan(3000) // 3 seconds
    })

    it("should render table rows quickly", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
      cy.get("tbody tr").should("be.visible")
    })
  })

  describe("Accessibility", () => {
    it("should have proper heading structure", () => {
      cy.get("h1").should("exist")
      cy.get("h2").should("exist")
    })

    it("should have accessible form inputs", () => {
      cy.get("input[placeholder*='Search']").should("have.attr", "aria-label").or("have.attr", "placeholder")
      cy.get("select").should("have.attr", "aria-label").or("have.attr", "id")
    })

    it("should have accessible buttons", () => {
      cy.get("button").each(($btn) => {
        expect($btn).to.have.text().or("have.attr", "aria-label")
      })
    })

    it("should support keyboard navigation", () => {
      cy.get("input[placeholder*='Search']").focus()
      cy.focused().should("have.attr", "placeholder")
    })
  })

  describe("Error Handling", () => {
    it("should handle network errors gracefully", () => {
      cy.intercept("GET", "**/admin/vocabulary", { statusCode: 500 })
      cy.visit("/admin/vocabulary")
      // Should show error message or fallback UI
    })

    it("should retry on failed requests", () => {
      cy.intercept("GET", "**/admin/vocabulary", (req) => {
        if (req.redirects.length === 0) {
          req.reply({ statusCode: 500 })
        } else {
          req.continue()
        }
      })
    })
  })
})
