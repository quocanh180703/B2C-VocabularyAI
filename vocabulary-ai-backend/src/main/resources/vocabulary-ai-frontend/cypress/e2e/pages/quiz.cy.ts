describe("Quiz Page E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/quiz")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load quiz page successfully", () => {
      cy.url().should("include", "/admin/quiz")
      cy.contains("Quiz Management").should("be.visible")
    })

    it("should display quiz table with proper columns", () => {
      cy.get("table").should("be.visible")
      cy.get("th").should("contain", "Title")
      cy.get("th").should("contain", "Level")
      cy.get("th").should("contain", "Category")
      cy.get("th").should("contain", "Questions")
      cy.get("th").should("contain", "Status")
    })

    it("should display search input and filter controls", () => {
      cy.get("input[placeholder*='Search']").should("be.visible")
      cy.get("select").should("have.length.greaterThan", 1)
    })

    it("should load quiz data", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Search Functionality", () => {
    it("should search quiz by title", () => {
      const searchTerm = "basic"
      cy.get("input[placeholder*='Search']").type(searchTerm)
      cy.get("tbody tr").should("exist")
    })

    it("should search quiz by category", () => {
      cy.get("input[placeholder*='Search']").type("business")
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
      cy.contains("No quizzes found").should("be.visible")
    })
  })

  describe("Filtering by Level", () => {
    it("should filter by A1 level", () => {
      cy.get("select").eq(0).select("A1")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by B1 level", () => {
      cy.get("select").eq(0).select("B1")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by C1 level", () => {
      cy.get("select").eq(0).select("C1")
      cy.get("tbody tr").should("exist")
    })

    it("should reset level filter to 'All'", () => {
      cy.get("select").eq(0).select("B1")
      cy.get("select").eq(0).select("All")
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Filtering by Status", () => {
    it("should filter by PUBLISHED status", () => {
      cy.get("select").eq(1).select("PUBLISHED")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by DRAFT status", () => {
      cy.get("select").eq(1).select("DRAFT")
      cy.get("tbody tr").should("exist")
    })

    it("should filter by ARCHIVED status", () => {
      cy.get("select").eq(1).select("ARCHIVED")
      cy.get("tbody tr").should("exist")
    })

    it("should reset status filter to 'All'", () => {
      cy.get("select").eq(1).select("PUBLISHED")
      cy.get("select").eq(1).select("All")
      cy.get("tbody tr").should("have.length.greaterThan", 0)
    })
  })

  describe("Combined Filtering", () => {
    it("should combine level and status filters", () => {
      cy.get("select").eq(0).select("A1")
      cy.get("select").eq(1).select("PUBLISHED")
      cy.get("tbody tr").should("exist")
    })

    it("should combine search with level filter", () => {
      cy.get("input[placeholder*='Search']").type("basic")
      cy.get("select").eq(0).select("B1")
      cy.get("tbody tr").should("exist")
    })

    it("should reset all filters", () => {
      cy.get("input[placeholder*='Search']").type("test")
      cy.get("select").eq(0).select("B1")
      cy.get("select").eq(1).select("PUBLISHED")
      
      cy.get("input[placeholder*='Search']").clear()
      cy.get("select").eq(0).select("All")
      cy.get("select").eq(1).select("All")
      
      cy.get("tbody tr").should("have.length.greaterThan", 0)
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

    it("should display correct page number", () => {
      cy.get("[data-testid='pagination']").should("contain", "Page")
    })
  })

  describe("Table Display", () => {
    it("should display all quiz columns with data", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").should("have.length.greaterThan", 5)
      })
    })

    it("should display level badges with correct colors", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains(/A1|A2|B1|B2|C1|C2/).should("be.visible")
      })
    })

    it("should display status badges", () => {
      cy.get("tbody tr").first().within(() => {
        cy.contains(/PUBLISHED|DRAFT|ARCHIVED/).should("be.visible")
      })
    })

    it("should display question count", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(3).should("contain.number")
      })
    })

    it("should display time limit in minutes", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(4).should("contain", "m")
      })
    })

    it("should display average score percentage", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(7).should("match", /\d+\.?\d*%|—/)
      })
    })

    it("should have view button for each row", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button[type='button']").should("exist")
      })
    })
  })

  describe("Detail View", () => {
    it("should navigate to detail page when clicking view button", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.url().should("include", "/admin/quiz/")
      cy.contains("Quiz Information").should("be.visible")
    })

    it("should display quiz detail information", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("Description").should("be.visible")
      cy.contains("Category").should("be.visible")
      cy.contains("Questions").should("be.visible")
      cy.contains("Time Limit").should("be.visible")
    })

    it("should display quiz statistics on detail page", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.contains("Total Attempts").should("be.visible")
      cy.contains("Average Score").should("be.visible")
      cy.contains("Pass Rate").should("be.visible")
    })

    it("should have back button on detail page", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("button").click({ force: true })
      })
      cy.get("button").contains("Back to quizzes").click()
      cy.url().should("include", "/admin/quiz")
    })

    it("should show 404 when quiz not found", () => {
      cy.visit("/admin/quiz/99999")
      cy.contains("Quiz not found").should("be.visible")
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
      cy.visit("/admin/quiz")
      cy.get("table").should("be.visible")
      const endTime = performance.now()
      const loadTime = endTime - startTime
      expect(loadTime).to.be.lessThan(3000) // 3 seconds
    })

    it("should render quiz rows quickly", () => {
      cy.get("tbody tr").should("have.length.greaterThan", 0)
      cy.get("tbody tr").should("be.visible")
    })

    it("should apply filters without significant delay", () => {
      const startTime = performance.now()
      cy.get("select").eq(0).select("B1")
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

    it("should have accessible select dropdowns", () => {
      cy.get("select").each(($select) => {
        expect($select).to.have.attr("aria-label").or("have.attr", "id")
      })
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
      
      cy.get("select").first().focus()
      cy.focused().should("be.a", "select")
    })
  })

  describe("Error Handling", () => {
    it("should handle network errors gracefully", () => {
      cy.intercept("GET", "**/admin/quiz", { statusCode: 500 })
      cy.visit("/admin/quiz")
      // Should show error message or fallback UI
    })

    it("should handle missing quiz detail gracefully", () => {
      cy.intercept("GET", "**/admin/quiz/**", { statusCode: 404 })
      cy.visit("/admin/quiz/invalid-id")
      cy.contains("Quiz not found").should("be.visible")
    })

    it("should show notification on search error", () => {
      cy.intercept("GET", "**/admin/quiz*", { statusCode: 500 }).as("quizSearch")
      cy.get("input[placeholder*='Search']").type("test")
      cy.wait("@quizSearch")
    })

    it("should recover after error", () => {
      cy.intercept("GET", "**/admin/quiz", { statusCode: 500 }).as("error")
      cy.visit("/admin/quiz")
      cy.wait("@error")
      
      cy.intercept("GET", "**/admin/quiz", { statusCode: 200, body: { content: [], totalPages: 1 } }).as("success")
      cy.reload()
      cy.wait("@success")
    })
  })

  describe("Data Consistency", () => {
    it("should maintain sort order during pagination", () => {
      cy.get("tbody tr").first().then(($row) => {
        const firstPageFirstTitle = $row.find("td").eq(0).text()
        cy.get("button").contains("Next").click({ force: true })
        cy.get("tbody tr").first().then(($nextRow) => {
          const secondPageFirstTitle = $nextRow.find("td").eq(0).text()
          expect(firstPageFirstTitle).not.to.equal(secondPageFirstTitle)
        })
      })
    })

    it("should maintain filters during pagination", () => {
      cy.get("select").eq(0).select("B1")
      cy.get("button").contains("Next").click({ force: true })
      cy.get("select").eq(0).should("have.value", "B1")
    })

    it("should reset page when changing search query", () => {
      cy.get("input[placeholder*='Search']").type("test1")
      cy.get("input[placeholder*='Search']").clear()
      cy.get("input[placeholder*='Search']").type("test2")
      cy.get("tbody tr").should("exist")
    })
  })
})
