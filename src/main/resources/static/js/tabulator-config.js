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

	if (document.getElementById('myLeavesTable')) {
		initializeMyLeavesTable();
	}


	if (document.getElementById('allLeavesTable')) {
		initializeAllLeavesTable();
	}

	function initializeAllLeavesTable() {
		console.log('Initializing All Employees Leave Balance table');
		try {
			var allLeavesTable = new Tabulator("#allLeavesTable", {
				ajaxURL: "/client/AllLeaveBalance/json",
				layout: "fitColumns",
				columns: [
					{ title: "Employee ID", field: "employeeId", width: 120 },
					{ title: "Employee Name", field: "employeeName", width: 160 },
					{ title: "Total General Leaves", field: "totalGeneralLeaves" },
					{ title: "General Availed", field: "generalLeavesAvailed" },
					{
						title: "General Balance",
						formatter: function(cell) {
							const d = cell.getRow().getData();
							return (d.totalGeneralLeaves || 0) - (d.generalLeavesAvailed || 0);
						}
					},
					{ title: "Total Privilege Leaves", field: "totalPrivilegeLeaves" },
					{ title: "Privilege Availed", field: "privilegeLeavesAvailed" },
					{
						title: "Privilege Balance",
						formatter: function(cell) {
							const d = cell.getRow().getData();
							return (d.totalPrivilegeLeaves || 0) - (d.privilegeLeavesAvailed || 0);
						}
					},
					{ title: "Total Restricted Holidays", field: "totalRestrictedHolidays" },
					{ title: "Restricted Availed", field: "restrictedHolidaysAvailed" },
					{
						title: "Restricted Balance",
						formatter: function(cell) {
							const d = cell.getRow().getData();
							return (d.totalRestrictedHolidays || 0) - (d.restrictedHolidaysAvailed || 0);
						}
					}
				],
				ajaxResponse: function(url, params, response) {
					console.log("=== TABULATOR DEBUG ===");
					console.log("Response:", response);

					// Handle string response
					if (typeof response === 'string') {
						try {
							response = JSON.parse(response);
						} catch (e) {
							console.error('Failed to parse All Leaves response as JSON:', e);
							showError('allLeavesTable', 'Invalid data received from server.');
							return [];
						}
					}

					// Handle error responses
					if (response && response.error) {
						showError('allLeavesTable', response.message || response.error);
						return [];
					}

					// Handle array response directly
					if (Array.isArray(response)) {
						hideError('errorMessage');
						return response;
					}

					// Handle object with data property
					if (response && Array.isArray(response.data)) {
						hideError('errorMessage');
						return response.data;
					}

					console.error("Unexpected response format:", response);
					showError('allLeavesTable', 'Unexpected data format received from server');
					return [];
				}
			});

			// Refresh button
			var refreshBtn = document.getElementById("refreshAll");
			if (refreshBtn) {
				refreshBtn.addEventListener("click", function() {
					console.log("Refreshing All Leaves table...");
					allLeavesTable.replaceData();
				});
			}

		} catch (error) {
			console.error('Error initializing All Leaves Tabulator:', error);
			showError('allLeavesTable', 'Error loading All Employees Leave Balance: ' + error.message);
		}
	}

	// Utility to hide error alert if needed
	function hideError(elementId) {
		var element = document.getElementById(elementId);
		if (element) {
			element.classList.add("d-none");
			element.innerText = "";
		}
	}
	function initializeMyLeavesTable() {
		console.log('Initializing My Leaves table');
		try {
			var myLeavesTable = new Tabulator("#myLeavesTable", {
				ajaxURL: '/client/myleaves/json',
				layout: "fitColumns",
				ajaxResponse: function(url, params, response) {
					console.log('My Leaves API response:', response);

					// Handle string response (parse JSON if needed)
					if (typeof response === 'string') {
						try {
							response = JSON.parse(response);
						} catch (e) {
							console.error('Failed to parse my leaves response:', e);
							return [];
						}
					}

					// Handle error responses
					if (response && response.error) {
						showError('myLeavesTable', 'Error: ' + response.error);
						return [];
					}

					// Handle single object response by wrapping it in array
					if (response && typeof response === 'object' && !Array.isArray(response)) {
						return [response]; // Wrap single object in array
					}

					// Handle array response
					if (Array.isArray(response)) {
						return response;
					}

					console.warn('Unexpected my leaves response format:', response);
					return [];
				},
				columns: [
					{ title: "Employee ID", field: "employeeId", width: 120 },
					{ title: "Employee Name", field: "employeeName", width: 160 },
					{ title: "Total General Leaves", field: "totalGeneralLeaves" },
					{ title: "General Availed", field: "generalLeavesAvailed" },
					{
						title: "General Balance",
						formatter: function(cell) {
							const data = cell.getRow().getData();
							return (data.totalGeneralLeaves || 0) - (data.generalLeavesAvailed || 0);
						}
					},
					{ title: "Total Privilege Leaves", field: "totalPrivilegeLeaves" },
					{ title: "Privilege Availed", field: "privilegeLeavesAvailed" },
					{
						title: "Privilege Balance",
						formatter: function(cell) {
							const data = cell.getRow().getData();
							return (data.totalPrivilegeLeaves || 0) - (data.privilegeLeavesAvailed || 0);
						}
					},
					{ title: "Total Restricted Holidays", field: "totalRestrictedHolidays" },
					{ title: "Restricted Availed", field: "restrictedHolidaysAvailed" },
					{
						title: "Restricted Balance",
						formatter: function(cell) {
							const data = cell.getRow().getData();
							return (data.totalRestrictedHolidays || 0) - (data.restrictedHolidaysAvailed || 0);
						}
					}
				]
			});

			var refreshBtn = document.getElementById('refreshMy');
			if (refreshBtn) {
				refreshBtn.addEventListener('click', function() {
					myLeavesTable.replaceData();
				});
			}

			myLeavesTable.on("dataLoaded", function(data) {
				console.log('My Leaves data loaded:', data);
				if (data && data.length === 0) {
					showError('myLeavesTable', 'No leave data found for your account.');
				}
			});

			myLeavesTable.on("ajaxError", function(error, response) {
				console.error('AJAX error loading my leaves:', error, response);
				if (response && response.status === 403) {
					showAccessDeniedMessage("Access denied. You do not have permission to view leave data.", 'myLeavesTable');
				} else {
					showError('myLeavesTable', 'Error loading your leave data. Please try again.');
				}
			});

		} catch (error) {
			console.error('Error initializing My Leaves Tabulator:', error);
			showError('myLeavesTable', 'Error loading your leave data: ' + error.message);
		}
	}


	function initializeEmployeeTable() {
		console.log('Initializing employee table');
		try {
			var employeeTable = new Tabulator("#employee-table", {
				ajaxURL: '/client/employees/json',
				layout: "fitColumns",
				pagination: "local",
				paginationSize: 10,
				ajaxLoading: true,
				ajaxResponse: function(url, params, response) {
					if (response && (response.error === "access_denied" || response.error === "insufficient_scope")) {
						showAccessDeniedMessage(response.message || response.error_description);
						return [];
					}
					return response;
				},
				columns: [
					{
						title: "ID",
						field: "id",
						width: 80,
						formatter: function(cell) {
							var value = cell.getValue();
							return '<a href="/client/employees/' + value +
								'" style="color:#007bff;text-decoration:underline;">' + value + '</a>';
						}
					},
					{ title: "Login", field: "login", headerFilter: "input" },
					{ title: "First Name", field: "firstName", headerFilter: "input" },
					{ title: "Last Name", field: "lastName", headerFilter: "input" },
					{ title: "Email", field: "email", headerFilter: "input", width: 200 }
				]
			});

			var refreshBtn = document.getElementById('refresh-table');
			if (refreshBtn) {
				refreshBtn.addEventListener('click', function() {
					employeeTable.setData();
				});
			}

			employeeTable.on("dataLoaded", function(data) {
				document.getElementById('row-count').textContent = data.length;
				document.getElementById('loading').style.display = 'none';

				if (data.length === 0) {
					var currentContent = document.getElementById('employee-table').innerHTML;
					if (!currentContent.includes('alert-warning')) {
						showAccessDeniedMessage("No data available. You may not have access to employee data.");
					}
				}
			});

			employeeTable.on("dataLoading", function() {
				document.getElementById('loading').style.display = 'block';
			});

			employeeTable.on("tableBuilt", function() {
				console.log('Employee table built successfully');
			});

			employeeTable.on("ajaxError", function(error, response) {
				console.error('AJAX error loading employees:', error, response);
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
			var employeeJsonRaw = typeof employeeJson !== 'undefined' ? employeeJson : '{}';
			var employeeIdValue = typeof employeeId !== 'undefined' ? employeeId : 'unknown';

			console.log('Raw employee JSON:', employeeJsonRaw);
			console.log('Employee ID:', employeeIdValue);

			var employeeData = {};
			if (typeof employeeJsonRaw === 'string') {
				try {
					var cleanJson = employeeJsonRaw.trim();
					if (cleanJson.startsWith('"') && cleanJson.endsWith('"')) {
						cleanJson = cleanJson.slice(1, -1).replace(/\\"/g, '"');
					}
					employeeData = JSON.parse(cleanJson);
				} catch (e) {
					console.error('Error parsing JSON string:', e);
					try {
						var jsonMatch = employeeJsonRaw.match(/\{.*\}/);
						if (jsonMatch) {
							employeeData = JSON.parse(jsonMatch[0]);
						} else {
							employeeData = { error: "Invalid JSON format", raw: employeeJsonRaw };
						}
					} catch (e2) {
						console.error('Second parsing attempt failed:', e2);
						employeeData = { error: "Failed to parse JSON", details: e2.message };
					}
				}
			} else {
				employeeData = employeeJsonRaw;
			}

			console.log('Parsed employee data:', employeeData);

			if (!employeeData || Object.keys(employeeData).length === 0 || employeeData.error) {
				showError('employee-detail-table',
					'No employee data available or invalid format. <a href="/client/employees">Return to list</a>');
				return;
			}

			var tableData = [];
			var standardFields = ['id', 'login', 'firstName', 'lastName', 'email'];

			standardFields.forEach(function(field) {
				if (employeeData[field] !== undefined) {
					tableData.push({
						field: field.charAt(0).toUpperCase() + field.slice(1),
						value: employeeData[field] || 'N/A'
					});
				}
			});

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
					tableData.push({ field: key, value: displayValue });
				}
			}

			var detailTable = new Tabulator("#employee-detail-table", {
				data: tableData,
				layout: "fitColumns",
				columns: [
					{ title: "Field", field: "field", width: 200, headerFilter: "input", headerSort: false },
					{
						title: "Value",
						field: "value",
						headerFilter: "input",
						formatter: function(cell) {
							var value = cell.getValue();
							if (value && value.length > 100) {
								return '<div style="max-height:200px;overflow-y:auto;white-space:pre-wrap;">' + value + '</div>';
							}
							return value;
						}
					}
				]
			});

			var backBtn = document.getElementById('back-button');
			if (backBtn) {
				backBtn.addEventListener('click', function() {
					window.location.href = '/client/employees';
				});
			}

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
				ajaxURL: '/client/localholidaylist/json',
				layout: "fitColumns",
				pagination: "local",
				paginationSize: 10,
				ajaxLoading: true,
				ajaxResponse: function(url, params, response) {
					console.log('Raw API response:', response);

					if (typeof response === 'string') {
						try {
							response = JSON.parse(response);
						} catch (e) {
							console.error('Failed to parse response as JSON:', e);
							return [];
						}
					}

					if (response && (response.error === "access_denied" || response.error === "insufficient_scope")) {
						showAccessDeniedMessage(response.message || response.error_description, 'leaves-table');
						return [];
					}

					// Handle array response
					if (Array.isArray(response)) {
						return response;
					}

					// Handle object with data property
					if (response && response.data) {
						return response.data;
					}

					console.warn('Unexpected response format:', response);
					return [];
				},
				columns: [
					{
						title: "Holiday Date",
						field: "holidayDate",
						sorter: "date",
						headerFilter: "input",
						// Add formatter to handle different date formats
						formatter: function(cell) {
							var value = cell.getValue();
							return value ? formatDate(value) : 'N/A';
						}
					},
					{
						title: "Day Name",
						field: "dayName",
						headerFilter: "input",
						// Fallback for different field names
						accessor: function(data) {
							return data.dayName || data.day || data.day_name || 'N/A';
						}
					},
					{
						title: "Holiday Type",
						field: "holidayType",
						headerFilter: "input",
						accessor: function(data) {
							return data.holidayType || data.type || data.holiday_type || 'N/A';
						}
					},
					{
						title: "Descriptions",
						field: "descriptions",
						headerFilter: "input",
						formatter: function(cell) {
							var value = cell.getValue();
							if (value && value.length > 50) {
								return '<span title="' + value + '">' + value.substring(0, 50) + '...</span>';
							}
							return value || 'N/A';
						},
						accessor: function(data) {
							return data.descriptions || data.description || data.desc || 'N/A';
						}
					}
				]
			});

			var refreshBtn = document.getElementById('refresh-table');
			if (refreshBtn) {
				refreshBtn.addEventListener('click', function() {
					leavesTable.setData();
				});
			}

			leavesTable.on("dataLoaded", function(data) {
				document.getElementById('row-count').textContent = data.length;
				document.getElementById('loading').style.display = 'none';
			});

			leavesTable.on("dataLoading", function() {
				document.getElementById('loading').style.display = 'block';
			});

			leavesTable.on("tableBuilt", function() {
				console.log('Leaves table built successfully');
			});

			leavesTable.on("ajaxError", function(error, response) {
				console.error('AJAX error loading leaves:', error, response);
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

	function showError(elementId, message) {
		var element = document.getElementById(elementId);
		if (element) {
			element.innerHTML = '<div class="alert alert-danger">' + message + '</div>';
		}
	}

	function formatDate(dateString) {
		if (!dateString) return 'N/A';
		try {
			var date = new Date(dateString);
			return date.toLocaleDateString();
		} catch (e) {
			return dateString;
		}
	}

	window.initializeEmployeeTable = initializeEmployeeTable;
	window.initializeEmployeeDetailTable = initializeEmployeeDetailTable;
	window.initializeLeavesTable = initializeLeavesTable;
	window.showAccessDeniedMessage = showAccessDeniedMessage;

});
