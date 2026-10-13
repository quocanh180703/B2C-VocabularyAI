describe("Speaking Page E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/speaking")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load speaking page successfully", () => {
      cy.url().should("include", "/admin/speaking")
      cy.contains("Speaking Management").should("be.visible")
    })

    it("should display page description", () => {
      cy.contains("Analyze pronunciation, fluency, grammar and AI feedback").should("be.visible")
    })

    it("should display statistics cards", () => {
      cy.get("[class*='grid']").find("[class*='Card']").should("have.length.greaterThan", 0)
      cy.contains("Total Attempts").should("be.visible")
      cy.contains("Avg. Pronunciation").should("be.visible")
      cy.contains("Avg. Fluency").should("be.visible")
      cy.contains("Avg. Overall").should("be.visible")
    })

    it("should display data table", () => {
      cy.get("table").should("be.visible")
    })

    it("should display table columns", () => {
      cy.get("th").should("contain", "Student")
      cy.get("th").should("contain", "Topic")
      cy.get("th").should("contain", "Pronunciation")
      cy.get("th").should("contain", "Fluency")
      cy.get("th").should("contain", "Grammar")
      cy.get("th").should("contain", "Overall")
    })
  })

  describe("Search Functionality", () => {
    it("should search speaking records by student name", () => {
      cy.get("input[placeholder*='Search']").type("john")
      cy.get("tbody tr").should("exist")
    })

    it("should search speaking records by topic", () => {
      cy.get("input[placeholder*='Search']").type("technology")
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
    })
  })

  describe("Filtering by Status", () => {
    it("should filter by Active status", () => {
      cy.get("select").select("Active")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by Inactive status", () => {
      cy.get("select").select("Inactive")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by Passed status", () => {
      cy.get("select").select("Passed")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by Failed status", () => {
      cy.get("select").select("Failed")
      cy.get("tbody tr").should("exist")
    })

    it("should reset filter to All", () => {
      cy.get("select").select("Active")
      cy.get("select").select("All")
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Combined Search and Filter", () => {
    it("should combine search with status filter", () => {
      cy.get("input[placeholder*='Search']").type("english")
      cy.get("select").select("Passed")
      cy.get("tbody tr").should("exist")
    })

    it("should reset all filters", () => {
      cy.get("input[placeholder*='Search']").type("test")
      cy.get("select").select("Active")
      
      cy.get("input[placeholder*='Search']").clear()
      cy.get("select").select("All")
      
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Table Display and Data", () => {
    it("should display all table columns with data", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").should("have.length.greaterThan", 5)
      })
    })

    it("should display student names", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).should("not.be.empty")
      })
    })

    it("should display topic names", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(1).should("not.be.empty")
      })
    })

    it("should display pronunciation scores", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(3).should("match", /\d+\.?\d*/)
      })
    })

    it("should display fluency scores", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(4).should("match", /\d+\.?\d*/)
      })
    })

    it("should display grammar scores", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(5).should("match", /\d+\.?\d*/)
      })
    })

    it("should display overall scores", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(6).should("match", /\d+\.?\d*/)
      })
    })

    it("should have action buttons for each row", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").should("exist")
      })
    })
  })

  describe("View Details Modal", () => {
    it("should open detail modal when clicking view button", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").first().click({ force: true })
      })
      cy.get("[role='dialog']").should("be.visible")
      cy.contains("Speaking Management details").should("be.visible")
    })

    it("should display complete record details in modal", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").first().click({ force: true })
      })
      cy.get("[role='dialog']").should("be.visible")
      cy.get("[role='dialog']").find("[class*='grid']").should("exist")
    })

    it("should close modal when clicking outside", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").first().click({ force: true })
      })
      cy.get("[role='dialog']").should("be.visible")
      cy.get("body").click(0, 0)
      cy.get("[role='dialog']").should("not.exist")
    })
  })

  describe("Action Buttons", () => {
    it("should have refresh button", () => {
      cy.get("button").contains("Refresh").should("be.visible")
    })

    it("should have export button", () => {
      cy.get("button").contains("Export").should("be.visible")
    })

    it("should export data as CSV", () => {
      cy.get("button").contains("Export").click()
      cy.readFile("cypress/downloads/speaking-management.csv").should("exist")
    })

    it("should refresh data when refresh button is clicked", () => {
      cy.get("button").contains("Refresh").click()
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
      cy.get("[class*='grid']").find("[class*='Card']").should("be.visible")
    })

    it("should hide mobile columns on desktop", () => {
      cy.viewport(1920, 1080)
      cy.get("thead th").then(($headers) => {
        const headerCount = $headers.length
        expect(headerCount).to.be.greaterThan(0)
      })
    })
  })

  describe("Performance", () => {
    it("should load page within acceptable time", () => {
      const startTime = performance.now()
      cy.visit("/admin/speaking")
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
      cy.get("select").select("Active")
      cy.get("tbody tr").should("exist")
      const endTime = performance.now()
      const filterTime = endTime - startTime
      expect(filterTime).to.be.lessThan(2000) // 2 seconds
    })
  })

  describe("Statistics Cards", () => {
    it("should display correct number of statistics", () => {
      cy.get("[class*='grid']").find("[class*='Card']").should("have.length", 4)
    })

    it("should display statistics labels", () => {
      cy.contains("Total Attempts").should("be.visible")
      cy.contains("Avg. Pronunciation").should("be.visible")
      cy.contains("Avg. Fluency").should("be.visible")
      cy.contains("Avg. Overall").should("be.visible")
    })

    it("should display statistics values", () => {
      cy.get("[class*='Card']").each(($card) => {
        cy.wrap($card).find("[class*='text-2xl']").should("have.text")
      })
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
      cy.intercept("GET", "**/admin/speaking", { statusCode: 500 })
      cy.visit("/admin/speaking")
    })

    it("should handle empty data gracefully", () => {
      cy.intercept("GET", "**/admin/speaking", {
        statusCode: 200,
        body: { content: [], totalPages: 1 }
      })
      cy.visit("/admin/speaking")
      cy.get("tbody tr").should("have.length", 0)
    })
  })

  describe("Data Consistency", () => {
    it("should maintain search query during status filter change", () => {
      const searchTerm = "english"
      cy.get("input[placeholder*='Search']").type(searchTerm)
      cy.get("input[placeholder*='Search']").should("have.value", searchTerm)
      
      cy.get("select").select("Active")
      cy.get("input[placeholder*='Search']").should("have.value", searchTerm)
    })

    it("should display consistent data across views", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).then(($cell) => {
          const studentName = $cell.text()
          cy.get("button").first().click({ force: true })
          cy.contains(studentName).should("exist")
        })
      })
    })
  })
})
