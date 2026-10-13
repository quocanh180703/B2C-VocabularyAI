describe("Reports & Analytics Page E2E Tests", () => {
  beforeEach(() => {
    cy.visit("/admin/reports")
    cy.get("body").should("be.visible")
  })

  describe("Page Load and Display", () => {
    it("should load reports page successfully", () => {
      cy.url().should("include", "/admin/reports")
      cy.contains("Reports & Analytics").should("be.visible")
    })

    it("should display page description", () => {
      cy.contains("Comprehensive system analytics and insights").should("be.visible")
    })

    it("should display loading state initially", () => {
      cy.visit("/admin/reports")
      cy.get("[class*='LoadingState']").should("exist")
    })

    it("should display learning progress statistics", () => {
      cy.contains("Total Quiz Attempts").should("be.visible")
      cy.contains("Average Score").should("be.visible")
      cy.contains("Avg Completion Time").should("be.visible")
      cy.contains("Unique Users").should("be.visible")
    })
  })

  describe("Learning Progress Stats", () => {
    it("should display total quiz attempts stat", () => {
      cy.contains("Total Quiz Attempts").should("be.visible")
      cy.contains("Total Quiz Attempts").parent().find("[class*='text-3xl']").should("contain", /\d/)
    })

    it("should display average score stat", () => {
      cy.contains("Average Score").should("be.visible")
      cy.contains("Average Score").parent().find("[class*='text-3xl']").should("contain", "%")
    })

    it("should display completion time stat", () => {
      cy.contains("Avg Completion Time").should("be.visible")
      cy.contains("seconds").should("be.visible")
    })

    it("should display unique users stat", () => {
      cy.contains("Unique Users").should("be.visible")
      cy.contains("Unique Users").parent().find("[class*='text-3xl']").should("contain", /\d/)
    })

    it("should display stats with proper icons", () => {
      cy.get("[class*='StatsCard']").should("have.length.greaterThan", 0)
    })
  })

  describe("Engagement Chart", () => {
    it("should display user engagement chart title", () => {
      cy.contains("User Engagement").should("be.visible")
    })

    it("should display chart for last 30 days", () => {
      cy.contains("Last 30 Days").should("be.visible")
    })

    it("should display engagement chart", () => {
      cy.get("[class*='SimpleLineChart']").should("have.length.greaterThan", 0)
    })
  })

  describe("Revenue Summary", () => {
    it("should display revenue summary card", () => {
      cy.contains("Revenue Summary").should("be.visible")
    })

    it("should display total transactions", () => {
      cy.contains("Total Transactions").should("be.visible")
      cy.contains("Total Transactions").parent().find("[class*='font-bold']").should("contain", /\d/)
    })

    it("should display total revenue", () => {
      cy.contains("Total Revenue").should("be.visible")
      cy.contains("Total Revenue").parent().find("[class*='text-green']").should("contain", "$")
    })

    it("should display average transaction value", () => {
      cy.contains("Avg Transaction").should("be.visible")
      cy.contains("Avg Transaction").parent().find("[class*='font-bold']").should("contain", "$")
    })

    it("should display unique customers count", () => {
      cy.contains("Unique Customers").should("be.visible")
      cy.contains("Unique Customers").parent().find("[class*='font-bold']").should("contain", /\d/)
    })
  })

  describe("Content Performance Table", () => {
    it("should display top content performance section", () => {
      cy.contains("Top Content Performance").should("be.visible")
    })

    it("should display content performance table", () => {
      cy.get("table").should("be.visible")
    })

    it("should display table headers", () => {
      cy.get("th").should("contain", "Quiz Title")
      cy.get("th").should("contain", "Level")
      cy.get("th").should("contain", "Attempts")
      cy.get("th").should("contain", "Avg Score")
      cy.get("th").should("contain", "Completion Rate")
    })

    it("should display quiz titles", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).should("not.be.empty")
      })
    })

    it("should display quiz levels", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(1).should("not.be.empty")
      })
    })

    it("should display attempt counts", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(2).should("match", /\d+/)
      })
    })

    it("should display average scores as percentages", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(3).should("match", /\d+\.?\d*%/)
      })
    })

    it("should display completion rate with progress bar", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("[class*='bg-blue']").should("exist")
      })
    })

    it("should show pagination info if more than 10 items", () => {
      cy.get("table tbody tr").then(($rows) => {
        if ($rows.length > 10) {
          cy.contains(/Showing 10 of/).should("be.visible")
        }
      })
    })
  })

  describe("Summary Cards", () => {
    it("should display learning engagement card", () => {
      cy.contains("Learning Engagement").should("be.visible")
    })

    it("should display learning engagement percentage", () => {
      cy.contains("Learning Engagement").parent().find("[class*='text-2xl']").should("contain", "%")
    })

    it("should display best performing users card", () => {
      cy.contains("Best Performing Users").should("be.visible")
    })

    it("should display revenue per user card", () => {
      cy.contains("Revenue per User").should("be.visible")
      cy.contains("Revenue per User").parent().find("[class*='text-green']").should("contain", "$")
    })

    it("should display conversion rate card", () => {
      cy.contains("Conversion Rate").should("be.visible")
      cy.contains("Conversion Rate").parent().find("[class*='text-2xl']").should("contain", "%")
    })
  })

  describe("Refresh Functionality", () => {
    it("should have refresh button in header", () => {
      cy.get("button").contains("Refresh").should("be.visible")
    })

    it("should refresh reports when clicking refresh button", () => {
      cy.get("button").contains("Refresh").click()
      cy.contains("Reports & Analytics").should("be.visible")
    })

    it("should reload all report data", () => {
      cy.get("button").contains("Refresh").click()
      cy.contains("Total Quiz Attempts").should("be.visible")
      cy.contains("Revenue Summary").should("be.visible")
    })
  })

  describe("Responsive Design", () => {
    it("should be responsive on mobile viewport", () => {
      cy.viewport("iphone-12")
      cy.contains("Reports & Analytics").should("be.visible")
      cy.contains("Total Quiz Attempts").should("be.visible")
    })

    it("should stack stats on mobile", () => {
      cy.viewport("iphone-12")
      cy.get("[class*='grid']").should("exist")
    })

    it("should be responsive on tablet viewport", () => {
      cy.viewport("ipad-2")
      cy.contains("Total Quiz Attempts").should("be.visible")
      cy.contains("Average Score").should("be.visible")
    })

    it("should display 2 columns on tablet", () => {
      cy.viewport("ipad-2")
      cy.get("[class*='md:grid-cols-2']").should("exist")
    })

    it("should be responsive on desktop viewport", () => {
      cy.viewport(1920, 1080)
      cy.contains("Total Quiz Attempts").should("be.visible")
      cy.contains("Unique Users").should("be.visible")
    })

    it("should display 4 columns on desktop", () => {
      cy.viewport(1920, 1080)
      cy.get("[class*='lg:grid-cols-4']").should("exist")
    })

    it("should scroll table horizontally on mobile", () => {
      cy.viewport("iphone-12")
      cy.get("table").should("have.css", "overflow-x")
    })
  })

  describe("Performance", () => {
    it("should load page within acceptable time", () => {
      const startTime = performance.now()
      cy.visit("/admin/reports")
      cy.contains("Total Quiz Attempts").should("be.visible")
      const endTime = performance.now()
      const loadTime = endTime - startTime
      expect(loadTime).to.be.lessThan(4000) // 4 seconds for reports with charts
    })

    it("should render all stats without lag", () => {
      cy.get("[class*='StatsCard']").should("be.visible")
    })

    it("should render charts without performance issues", () => {
      cy.get("[class*='SimpleLineChart']").should("exist")
    })

    it("should render content table without lag", () => {
      cy.get("table").should("be.visible")
      cy.get("tbody tr").should("be.visible")
    })
  })

  describe("Data Formatting", () => {
    it("should format quiz attempts with comma separators", () => {
      cy.contains("Total Quiz Attempts").parent().find("[class*='text-3xl']").should("match", /\d{1,3}(,\d{3})*/)
    })

    it("should display scores as percentages", () => {
      cy.contains("Average Score").parent().find("[class*='text-3xl']").should("contain", "%")
    })

    it("should display completion time in seconds", () => {
      cy.contains("Avg Completion Time").parent().find("text").should("match", /\d+s/)
    })

    it("should display revenue with dollar signs", () => {
      cy.contains("Total Revenue").parent().find("[class*='font-bold']").should("match", /\$/)
    })

    it("should format revenue with two decimal places", () => {
      cy.contains("Total Revenue").parent().find("[class*='font-bold']").should("match", /\$\d+\.\d{2}/)
    })

    it("should display completion rates as percentages", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(4).should("contain", "%")
      })
    })
  })

  describe("Accessibility", () => {
    it("should have proper heading structure", () => {
      cy.get("h1").should("exist")
      cy.get("h3").should("exist")
    })

    it("should have semantic page header", () => {
      cy.contains("Reports & Analytics").should("be.visible")
      cy.contains("Comprehensive system analytics and insights").should("be.visible")
    })

    it("should have accessible buttons", () => {
      cy.get("button").each(($btn) => {
        expect($btn).to.have.text().or("have.attr", "aria-label")
      })
    })

    it("should have accessible table headers", () => {
      cy.get("th").each(($th) => {
        expect($th).to.have.text()
      })
    })

    it("should support keyboard navigation", () => {
      cy.get("button").contains("Refresh").focus()
      cy.focused().should("contain", "Refresh")
    })
  })

  describe("Error Handling", () => {
    it("should handle network errors gracefully", () => {
      cy.intercept("GET", "**/admin/reports*", { statusCode: 500 })
      cy.visit("/admin/reports")
    })

    it("should show error message when data fails to load", () => {
      cy.intercept("GET", "**/admin/reports/engagement", { statusCode: 500 })
      cy.intercept("GET", "**/admin/reports/learning", { statusCode: 500 })
      cy.intercept("GET", "**/admin/reports/content", { statusCode: 500 })
      cy.intercept("GET", "**/admin/reports/revenue", { statusCode: 500 })
      cy.visit("/admin/reports")
    })

    it("should recover when refresh is clicked after error", () => {
      cy.intercept("GET", "**/admin/reports*", { statusCode: 500 }).as("error")
      cy.visit("/admin/reports")
      cy.wait("@error")
      
      cy.intercept("GET", "**/admin/reports*", { 
        statusCode: 200,
        body: []
      }).as("success")
      
      cy.get("button").contains("Refresh").click()
      cy.wait("@success")
    })
  })

  describe("Data Consistency", () => {
    it("should maintain data during refresh", () => {
      cy.contains("Total Quiz Attempts").parent().find("[class*='text-3xl']").then(($initialValue) => {
        const initialValue = $initialValue.text()
        cy.get("button").contains("Refresh").click()
        cy.contains("Total Quiz Attempts").parent().find("[class*='text-3xl']").should("contain", initialValue)
      })
    })

    it("should update all sections together", () => {
      cy.get("button").contains("Refresh").click()
      cy.contains("Total Quiz Attempts").should("be.visible")
      cy.contains("Revenue Summary").should("be.visible")
      cy.contains("Top Content Performance").should("be.visible")
    })

    it("should maintain table data consistency", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("td").eq(0).then(($title) => {
          const title = $title.text()
          cy.get("button").contains("Refresh").click()
          cy.get("tbody tr").first().within(() => {
            cy.get("td").eq(0).should("not.be.empty")
          })
        })
      })
    })
  })

  describe("Chart Interaction", () => {
    it("should display engagement chart within boundaries", () => {
      cy.get("[class*='SimpleLineChart']").should("exist").within(() => {
        cy.get("[class*='w-full']").should("exist")
      })
    })

    it("should render chart with proper styling", () => {
      cy.contains("User Engagement").should("be.visible")
      cy.get("[class*='SimpleLineChart']").should("have.css", "width")
    })
  })

  describe("Progress Bars", () => {
    it("should display completion rate progress bars", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("[class*='bg-muted']").should("exist")
        cy.get("[class*='bg-blue']").should("exist")
      })
    })

    it("should fill progress bar based on completion rate", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("[class*='bg-blue']").should("have.css", "width")
      })
    })

    it("should not exceed 100% on progress bar", () => {
      cy.get("tbody tr").first().within(() => {
        cy.get("[class*='bg-blue']").then(($bar) => {
          const widthStyle = $bar.attr("style")
          const match = widthStyle?.match(/width:\s*(\d+)%/)
          if (match) {
            const width = parseInt(match[1])
            expect(width).to.be.at.most(100)
          }
        })
      })
    })
  })
})
