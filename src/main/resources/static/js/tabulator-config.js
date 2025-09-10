document.addEventListener('DOMContentLoaded', function() {
    console.log('Tabulator config loaded');
    
    if (document.getElementById('employee-table')) {
        initializeEmployeeTable();
    }
    
    if (document.getElementById('employee-detail-table')) {
        initializeEmployeeDetailTable();
    }
    
    if (document.getElementById('leaves-table')) {
        initializeLeavesTable();
    }
});

function initializeEmployeeTable() {
    console.log('Initializing employee table');
    
    try {
        var employeeTable = new Tabulator("#employee-table", {
            ajaxURL: '/client/employees/json',
            layout: "fitColumns",
            pagination: "local",
            paginationSize: 10,
            ajaxLoading: true,
            // ADDED: Handle access denied responses
            ajaxResponse: function(url, params, response) {
                // Check if response contains access denied error
                if (response && (response.error === "access_denied" || response.error === "insufficient_scope")) {
                    showAccessDeniedMessage(response.message || response.error_description);
                    return []; // Return empty array to prevent table errors
                }
                return response;
            },
            columns: [
                {
                    title: "ID", 
                    field: "id", 
                    width: 80,
                    formatter: function(cell, formatterParams, onRendered) {
                        var value = cell.getValue();
                        return '<a href="/client/employees/' + value + '" style="color: #007bff; text-decoration: underline;">' + value + '</a>';
                    }
                },
                {title: "Login", field: "login", headerFilter: "input"},
                {title: "First Name", field: "firstName", headerFilter: "input"},
                {title: "Last Name", field: "lastName", headerFilter: "input"},
                {title: "Email", field: "email", headerFilter: "input", width: 200}
            ]
        });

        // Refresh button functionality
        var refreshBtn = document.getElementById('refresh-table');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', function() {
                employeeTable.setData();
            });
        }

        // Add event listeners for table events
        employeeTable.on("dataLoaded", function(data){
            document.getElementById('row-count').textContent = data.length;
            document.getElementById('loading').style.display = 'none';
            
            // ADDED: Check if data is empty due to access denial
            if (data.length === 0) {
                var currentContent = document.getElementById('employee-table').innerHTML;
                if (!currentContent.includes('alert-warning')) {
                    showAccessDeniedMessage("No data available. You may not have access to employee data.");
                }
            }
        });
        
        employeeTable.on("dataLoading", function(){
            document.getElementById('loading').style.display = 'block';
        });
        
        employeeTable.on("tableBuilt", function(){
            console.log('Employee table built successfully');
        });
        
        // ADDED: Handle AJAX errors specifically
        employeeTable.on("ajaxError", function(error, response){
            console.error('AJAX error loading employees:', error, response);
            
            // Check if it's a 403 Forbidden error
            if (response && response.status === 403) {
                showAccessDeniedMessage("Access denied. You do not have permission to view employee data.");
            } else {
                showError('employee-table', 'Error loading employee data. Please try again.');
            }
        });

    } catch (error) {
        console.error('Error initializing Employee Tabulator:', error);
        showError('employee-table', 'Error loading employee data: ' + error.message);
    }
}

function initializeEmployeeDetailTable() {
    console.log('Initializing employee detail table');
    
    try {
        // Get the employee JSON data from Thymeleaf attribute
        var employeeJsonRaw = typeof employeeJson !== 'undefined' ? employeeJson : '{}';
        var employeeIdValue = typeof employeeId !== 'undefined' ? employeeId : 'unknown';
        
        console.log('Raw employee JSON:', employeeJsonRaw);
        console.log('Employee ID:', employeeIdValue);
        
        var employeeData = {};
        
        // Parse the JSON if it's a string
        if (typeof employeeJsonRaw === 'string') {
            try {
                // Clean the JSON string first
                var cleanJson = employeeJsonRaw.trim();
                if (cleanJson.startsWith('"') && cleanJson.endsWith('"')) {
                    cleanJson = cleanJson.slice(1, -1).replace(/\\"/g, '"');
                }
                employeeData = JSON.parse(cleanJson);
            } catch (e) {
                console.error('Error parsing JSON string:', e);
                // Try to handle malformed JSON
                try {
                    // Extract JSON from potentially problematic string
                    var jsonMatch = employeeJsonRaw.match(/\{.*\}/);
                    if (jsonMatch) {
                        employeeData = JSON.parse(jsonMatch[0]);
                    } else {
                        employeeData = {error: "Invalid JSON format", raw: employeeJsonRaw};
                    }
                } catch (e2) {
                    console.error('Second parsing attempt failed:', e2);
                    employeeData = {error: "Failed to parse JSON", details: e2.message};
                }
            }
        } else {
            employeeData = employeeJsonRaw;
        }
        
        console.log('Parsed employee data:', employeeData);
        
        // Check if we have valid data
        if (!employeeData || Object.keys(employeeData).length === 0 || employeeData.error) {
            showError('employee-detail-table', 
                'No employee data available or invalid format. <a href="/client/employees">Return to list</a>');
            return;
        }
        
        // Create table data from employee object
        var tableData = [];
        var standardFields = ['id', 'login', 'firstName', 'lastName', 'email'];
        
        // Add standard fields first
        standardFields.forEach(function(field) {
            if (employeeData[field] !== undefined) {
                tableData.push({
                    field: field.charAt(0).toUpperCase() + field.slice(1),
                    value: employeeData[field] || 'N/A'
                });
            }
        });
        
        // Add any additional fields that might be present
        for (var key in employeeData) {
            if (employeeData.hasOwnProperty(key) && !standardFields.includes(key)) {
                var value = employeeData[key];
                var displayValue;
                
                if (value === null || value === undefined) {
                    displayValue = 'N/A';
                } else if (typeof value === 'object') {
                    displayValue = JSON.stringify(value, null, 2);
                } else {
                    displayValue = value.toString();
                }
                
                tableData.push({
                    field: key,
                    value: displayValue
                });
            }
        }
        
        // Initialize Tabulator
        var detailTable = new Tabulator("#employee-detail-table", {
            data: tableData,
            layout: "fitColumns",
            columns: [
                {
                    title: "Field", 
                    field: "field", 
                    width: 200, 
                    headerFilter: "input",
                    headerSort: false
                },
                {
                    title: "Value", 
                    field: "value", 
                    headerFilter: "input", 
                    formatter: function(cell, formatterParams, onRendered) {
                        var value = cell.getValue();
                        if (value && value.length > 100) {
                            return '<div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap;">' + value + '</div>';
                        }
                        return value;
                    }
                }
            ]
        });
        
        // Add a back button functionality
        var backBtn = document.getElementById('back-button');
        if (backBtn) {
            backBtn.addEventListener('click', function() {
                window.location.href = '/client/employees';
            });
        }
        
        // Add edit button functionality if needed
        var editBtn = document.getElementById('edit-button');
        if (editBtn) {
            editBtn.addEventListener('click', function() {
                alert('Edit functionality would go here for employee ID: ' + employeeIdValue);
            });
        }
        
    } catch (error) {
        console.error('Error initializing employee detail Tabulator:', error);
        showError('employee-detail-table', 
            'Error loading employee details: ' + error.message + '. <a href="/client/employees">Return to list</a>');
    }
}

function initializeLeavesTable() {
    console.log('Initializing Leaves table');
    
    try {
        var leavesTable = new Tabulator("#leaves-table", {
            ajaxURL: '/client/leaves/json',
            layout: "fitColumns",
            pagination: "local",
            paginationSize: 10,
            ajaxLoading: true,
            // ADDED: Handle access denied for leaves as well
            ajaxResponse: function(url, params, response) {
                if (response && (response.error === "access_denied" || response.error === "insufficient_scope")) {
                    showAccessDeniedMessage(response.message || response.error_description, 'leaves-table');
                    return [];
                }
                return response;
            },
            columns: [
                {
                    title: "Holiday Date", 
                    field: "holidayDate", 
                    sorter: "date",
                    headerFilter: "input"
                },
                {
                    title: "Day Name", 
                    field: "dayName", 
                    headerFilter: "input"
                },
                {
                    title: "Holiday Type", 
                    field: "holidayType", 
                    headerFilter: "input"
                },
                {
                    title: "Descriptions", 
                    field: "descriptions", 
                    headerFilter: "input",
                    formatter: function(cell, formatterParams, onRendered) {
                        var value = cell.getValue();
                        if (value && value.length > 50) {
                            return '<span title="' + value + '">' + value.substring(0, 50) + '...</span>';
                        }
                        return value;
                    }
                }
            ]
        });

        // Refresh button functionality
        var refreshBtn = document.getElementById('refresh-table');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', function() {
                leavesTable.setData();
            });
        }

        // Add event listeners for table events
        leavesTable.on("dataLoaded", function(data){
            document.getElementById('row-count').textContent = data.length;
            document.getElementById('loading').style.display = 'none';
        });
        
        leavesTable.on("dataLoading", function(){
            document.getElementById('loading').style.display = 'block';
        });
        
        leavesTable.on("tableBuilt", function(){
            console.log('Leaves table built successfully');
        });

        // Add error handling
        leavesTable.on("ajaxError", function(error, response){
            console.error('AJAX error loading leaves:', error, response);
            
            // Check if it's a 403 Forbidden error
            if (response && response.status === 403) {
                showAccessDeniedMessage("Access denied. You do not have permission to view leaves data.", 'leaves-table');
            } else {
                showError('leaves-table', 'Error loading leaves data. Please try again.');
            }
        });

    } catch (error) {
        console.error('Error initializing Leaves Tabulator:', error);
        showError('leaves-table', 'Error initializing leaves table: ' + error.message);
    }
}

// NEW FUNCTION: Show access denied message
function showAccessDeniedMessage(message, elementId = 'employee-table') {
    var errorHtml = `
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
            <h4 class="alert-heading">Access Denied</h4>
            <p>${message || 'You do not have permission to access this data.'}</p>
            <hr>
            <p class="mb-0">This section is restricted to Management HR team members only.</p>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    var element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = errorHtml;
    }
}

// EXISTING FUNCTION: Show error message
function showError(elementId, message) {
    var element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="alert alert-danger">' + message + '</div>';
    }
}

// Utility function to format dates
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        var date = new Date(dateString);
        return date.toLocaleDateString();
    } catch (e) {
        return dateString;
    }
}

// Export functions for global access (if needed)
window.initializeEmployeeTable = initializeEmployeeTable;
window.initializeEmployeeDetailTable = initializeEmployeeDetailTable;
window.initializeLeavesTable = initializeLeavesTable;
window.showAccessDeniedMessage = showAccessDeniedMessage;