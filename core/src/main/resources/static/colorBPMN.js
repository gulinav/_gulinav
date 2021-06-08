//This is the BPMN modeler
let viewerColored = new BpmnJS({
    container: '#canvasColored'
});

function uuidv4() {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

function createTableRow(val) {
    let result = document.createElement("input");
    result.setAttribute("type", "text");
    result.setAttribute("size", "30");
    result.setAttribute("value", val);
    let resultTextNode = document.createTextNode(val);
    result.appendChild(resultTextNode);
    return result;
}

function createDataEntry(code, timestamp, value) {
    let tableRowElem = document.createElement("tr");
    let tableDataElem1 = document.createElement("td");
    let tableDataElem2 = document.createElement("td");
    let tableDataElem3 = document.createElement("td");
    let inputElem1 = createTableRow(code);
    let inputElem2 = createTableRow(timestamp);
    let inputElem3 = createTableRow(value);

    tableRowElem.appendChild(tableDataElem1);
    tableRowElem.appendChild(tableDataElem2);
    tableRowElem.appendChild(tableDataElem3);
    tableDataElem1.appendChild(inputElem1);
    tableDataElem2.appendChild(inputElem2);
    tableDataElem3.appendChild(inputElem3);
    return tableRowElem;
}

function clearDataTable() {
    let dataTable = document.getElementById("dataTable");
    let header = document.getElementById("table-header");
    while (dataTable.firstChild) {
        dataTable.removeChild(dataTable.lastChild)
    }
    dataTable.appendChild(header);
}

function setMockData() {
    clearDataTable();
    let dataTable = document.getElementById("dataTable")
    dataTable.appendChild(createDataEntry("MISC:AUREUS_OR_LUGDUNENSIS", (new Date().toISOString()),
    "false"));
    dataTable.appendChild(createDataEntry("MISC:MULTIPLE_SAMPLES", (new Date().toISOString()),
    "false"));
    dataTable.appendChild(createDataEntry("MISC:ANOTHER_SAMPLE_TAKEN", (new Date().toISOString()), "true"))
    dataTable.appendChild(createDataEntry("MISC:BOTH_POSITIVE", (new Date().toISOString()), ""))
}

setMockData()

//This is the 'Upload Guideline' button
const input = document.getElementById('guideline');
//This is the table
const table = document.getElementById('dataTable');
//This is the selection list
const selectionList = document.getElementById('list');
//This is the 'Show Guideline' button
// const showButton = document.getElementById('show');
//This is the 'Clear' button for the modeler
const clearDButton = document.getElementById('clearDiagram');
//This is the 'Evaluate' button
const evalButton = document.getElementById('evaluate');
//This is the 'Add Row'-button
const addRowButton = document.getElementById('addRow');
//This is the 'Clear' button for the table
const clearTButton = document.getElementById('clearText');
//This is the 'Save as SVG' button
const svgButton = document.getElementById('js-download-svg');
//This is the container for the simplified guidelineItems on the right
const guidelineContainer = document.getElementById('guidelineContainer');
const resetMockDataBtn = document.getElementById('resetMockButton');

// Only needed if frontend is served from somewhere else.. otherwise leave empty
const BACKEND_URL = ''

selectionList.addEventListener("change", function () {
    for (i in guidelines) {
        if (selectionList.value === guidelines[i][0]) {
            updateSelectedGuideline(selectionList.value, guidelines[i][1]);
        }
    }
})


//Array with all available guidelines, the current guideline and its Id
let guidelines = [],
    currentGuideline = '';
currentGuidelineId = '';

downloadDemoGuideline();

function downloadDemoGuideline() {
    let demoGuidelineRequest = new XMLHttpRequest();
    demoGuidelineRequest.onreadystatechange = function () {
        demoGuidelineRequest.onreadystatechange = function () {
            //Request successful
            if (this.readyState === 4) {
                if (this.status === 200) {
                    console.log("demoguideline", this.responseText);
                    addGuideline("demo", this.responseText);
                    updateSelectedGuideline("demo", this.responseText);
                } else {
                    console.error("Fail: Backend broken")
                }
            }
        };
    }
    demoGuidelineRequest.open('GET', BACKEND_URL + '/rest/getDemoGuideline');
    demoGuidelineRequest.send();
}

function onClickDemoGuideline() {
    window.open(BACKEND_URL + "/rest/getDemoGuideline")
}


//Create the body of the 'Upload Guideline' POST request
function createUploadReq(id, file) {
    return JSON.stringify({id: id, guideline: file});
}

//Reset file input to detect upload of identical files
input.onclick = function () {
    this.value = null;
}

//Event listener for the file input            background-color: grey;
input.addEventListener('change', function () {
    //Read the input file
    const reader = new FileReader();
    reader.readAsText(input.files[0]);
    reader.onload = function () {
        let file = reader.result;
        //Extract the name of the file without ending
        let fileId = input.files[0].name.slice(0, -5) + ('/') + uuidv4();
        //Check if a guideline with the same name has already been uploaded
        for (i in guidelines) {
            if (fileId === guidelines[i][0]) {
                alert('A guideline with the name "' + fileId + '" has already been uploaded.');
                return;
            }
        }
        uploadGuideline(fileId, file)
    }
}, false);

function addGuideline(id, guideline) {
    //Add the uploaded file to the list of guidelines
    guidelines.push([id, guideline]);
    currentGuideline = guideline;
    currentGuidelineId = id;
    //Add uploaded guideline to selection list
    let option = document.createElement('option');
    option.text = currentGuidelineId;
    option.value = currentGuidelineId;
    selectionList.add(option);
}

function updateSelectedGuideline(guidelineId, guideline) {
    //Set uploaded guideline as selected option
    selectionList.value = guidelineId;
    //Import the input file in the modeler
    viewerColored.importXML(guideline, function (err) {
        if (err) {
            //Something went wrong
            console.log('Import failed');
            return;
        }
        //Optimize view
        let canvas = viewerColored.get('canvas');
        canvas.zoom('fit-viewport');
    });
    updateSelectedGuideline2();
}

function uploadGuideline(guidelineId, guideline) {
    //Create a new HTTP request
    let uploadReq = new XMLHttpRequest();
    uploadReq.withCredentials = true;
    //Wait for response of the backend
    uploadReq.onreadystatechange = function () {
        //Request successful
        if (this.readyState === 4) {
            if (this.status === 200) {
                addGuideline(guidelineId, guideline);
                //Set uploaded guideline as selected option
                updateSelectedGuideline(guidelineId, guideline);
            }
            //Request to backend failed
            else {
                alert('Upload failed. Check the format of the guideline. StatusCode was' + this.status)
            }
        }
    };
    //Fill and send the request to backend
    let data = createUploadReq(guidelineId, guideline);
    //For docker: http://192.168.99.100:8080
    uploadReq.open('POST', BACKEND_URL + '/rest/guideline');
    uploadReq.setRequestHeader('Content-Type', 'application/json');
    uploadReq.send(data);
}

function updateSelectedGuideline2() {
    //If nothing has been chosen leave everything as it is
    if (selectionList.selectedIndex <= 0) {
        //Continue to display current guideline
        selectionList.value = currentGuidelineId;
        return;
    }
    //ID of the selected guideline
    let sel = selectionList.value;
    //Set the current guideline to the selected one
    currentGuidelineId = sel;
    currentGuideline = guidelines[selectionList.selectedIndex - 1][1];
    //Import the selected guideline in the modeler
    viewerColored.importXML(currentGuideline, function (err) {
        if (err) {
            //Something went wrong
            console.log('Import failed');
            alert('Import failed. Check the format of the guideline.');
            return;
        }
        //Optimize view
        let canvas = viewerColored.get('canvas');
        canvas.zoom('fit-viewport');
    });
}

// //Show the selected guideline
// showButton.addEventListener('click', function () {
//     updateSelectedGuideline();
// });

//Downloads the BPMN file as SVG
function downloadSvg(s) {
    let svgBlob = new Blob([s], {
        type: 'image/svg+xml'
    });
    //If no guideline is displayed, the name of the SVG is svg.svg
    let fileName = currentGuidelineId.concat('.svg');
    let downloadLink = document.createElement('a');
    downloadLink.download = fileName;
    //downloadLink.innerHTML = 'Get BPMN SVG';
    downloadLink.href = window.URL.createObjectURL(svgBlob);
    downloadLink.onclick = function (event) {
        document.body.removeChild(event.target);
    };
    downloadLink.style.visibility = 'hidden';
    document.body.appendChild(downloadLink);
    downloadLink.click();
}

//Export the displayed diagram as SVG
svgButton.addEventListener('click', function () {
    //Save function of the modeler
    viewerColored.saveSVG({format: true}, function (err, svg) {
        if (err) {
            //Something went wrong
            console.log('Save failed');
            alert('The file could not be saved.');
            return;
        }
        //Download the SVG
        downloadSvg(svg);
    });
});

//Clear the viewer
clearDButton.addEventListener('click', function () {
    //Remove the guideline from the viewer
    viewerColored.clear();
    //No current guideline
    currentGuideline = '';
    currentGuidelineId = '';
    selectionList.selectedIndex = '0'
});

//Add a new to the table
addRowButton.addEventListener('click', function () {
    //Add a new row
    let newRow = table.insertRow(table.rows.length);
    for (i = 0; i < 3; i++) {
        //Add 3 cells per row
        let newCell = newRow.insertCell(i);
        //Create text input fields
        let field = document.createElement('INPUT');
        field.setAttribute('type', 'text');
        field.setAttribute('size', '30');
        //Insert input fields in the cells
        newCell.appendChild(field);
    }
})


//Color tasks according to the response of the backend
function setColor(obj) {
    //Access the modeling properties of the modeler
    let elementRegistry = viewerColored.get('elementRegistry'),
        modeling = viewerColored.get('modeling'),
        task;
    //Get a list of all IDs from all tasks of the current guideline
    let allIDs = [];
    let parser = new DOMParser();
    let guidelineXML = parser.parseFromString(currentGuideline, "text/xml");
    let allTasks = Array.from(guidelineXML.getElementsByTagName("bpmn:userTask")).concat(Array.from(guidelineXML.getElementsByTagName("bpmn:serviceTask")));
    for (x of allTasks) {
        allIDs.push(x.id);
    }
    //Loop through all tasks of the given JSON object
    for (x of obj.guidelineItems) {
        //Get the tasks by their IDs
        task = elementRegistry.get(x.taskId)
        //If an ID is present in the JSON object(=reachable), delete it in 'allIDs'
        allIDs.splice(allIDs.indexOf(x.taskId), 1);
        //Color the tasks according to their time phase
        switch (x.timePhase) {
            case "PAST":
                //Blue
                console.log('blue: ' + x.taskId);
                modeling.setColor([task], {
                    stroke: 'rgb(0, 51, 153)',
                    fill: 'rgba(0, 51, 153, 0.2)'
                });
                break;
            case "PRESENT":
                //Red
                console.log('red: ' + x.taskId);
                modeling.setColor([task], {
                    stroke: 'rgb(179, 0, 0)',
                    fill: 'rgba(179, 0, 0, 0.2)'
                });
                break;
            case "FUTURE":
                //Green
                console.log('green: ' + x.taskId);
                modeling.setColor([task], {
                    stroke: 'rgb(45, 134, 85)',
                    fill: 'rgba(45, 134, 85, 0.2)'
                });
                break;
        }
    }
    //All elements that weren't in the JSON object are unreachable
    console.log('grey: ' + allIDs);
    for (x of allIDs) {
        task = elementRegistry.get(x);
        //Grey
        modeling.setColor([task], {
            stroke: 'rgb(77, 77, 77)',
            fill: 'rgb(77, 77, 77, 0.2)'
        });
    }
}

//Only needed for testing
let testpayload = '[{"code": "GPM_CS:LABORATORY", "values": [{"time": "2011-10-05T14:48:00.000Z","value": 10000}]}]'

function updateSimplifiedGuidelineContainer(guidelineItems) {
    console.log("Guideline Items to update container:", guidelineItems);
    while (guidelineContainer.firstChild) {
        guidelineContainer.removeChild(guidelineContainer.lastChild);
    }
    if (document.getElementById('simplified-info-text')) {
        document.getElementById('simplified-info-text').remove()
    }
    for (item of guidelineItems.guidelineItems) {
        let guidelineItemDiv = document.createElement('div');
        guidelineItemDiv.classList.add("guidelineItem")
        guidelineItemDiv.textContent = item['shortDesc']
        switch (item.timePhase) {
            case "PAST": {
                guidelineItemDiv.classList.add("past")
                break;
            }
            case "PRESENT": {
                guidelineItemDiv.classList.add("present")
                break;
            }
            case "FUTURE": {
                guidelineItemDiv.classList.add("future")
                break;
            }
            default: {
                guidelineItemDiv.classList.add("default")
                break;
            }
        }
        guidelineContainer.appendChild(guidelineItemDiv)
    }
}

//Create the body of the "evaluate patient" POST request
function createEvalReq(p) {
    let payload = [];
    //If there is no (useful) user input, use empty patient data
    if (p.length) {
        //Loop through all elements of p
        for (let i = 0; i < p.length; i++) {
            //This is a new payload object
            let json = {code: p[i].code, values: [{time: p[i].timestamp, value: p[i].value}]}
            let flag = 0;
            //Loop through all elements that are already in 'payload'
            for (let j = 0; j < payload.length; j++) {
                //Check if a code already exists
                if (p[i].code === payload[j].code) {
                    //Add timestamp and value to value list of existing code
                    payload[j].values.push({time: p[i].timestamp, value: p[i].value})
                    //'flag' is 1 if acode already exists
                    flag = 1;
                    break;
                }
            }
            //only add the new payload object if code is new
            if (flag === 0) {
                payload.push(json);
            }
        }
        //Sort the value array of each code according to the timestamp
        for (let i = 0; i < payload.length; i++) {
            payload[i].values.sort(function (a, b) {
                let dateA = new Date(a.time), dateB = new Date(b.time);
                return dateA - dateB;
            });
        }
    }
    //Convert JSON to a string
    return JSON.stringify({guidelineId: currentGuidelineId, patientPayload: payload});
}

//Event listener for the 'Evaluate' button
evalButton.addEventListener('click', function () {
    //Check if a guideline has been selected
    if (currentGuideline === '' && currentGuidelineId === '') {
        alert('Choose a guideline first');
        return;
    }
    //Collect all input values of the table
    let payload = [];
    for (let i = 1; i < table.rows.length; i++) {
        //Add an object per row
        payload.push({
            code: table.rows[i].cells[0].children[0].value.trim(),
            timestamp: table.rows[i].cells[1].children[0].value.trim(),
            value: table.rows[i].cells[2].children[0].value.trim()
        })
    }
    //Loop through all objects
    for (let i = 0; i < payload.length; i++) {
        //Discard object if at least one entry in a row is empty
        if (payload[i].code === '' || payload[i].timestamp === '' || payload[i].value === '') {
            //Remove from payload
            payload.splice(i, 1);
            //Stay at same index because of removal
            i--;
        }
    }
    //Create a new HTTP request
    let evalReq = new XMLHttpRequest();
    evalReq.withCredentials = true;
    //Wait for a response of the backend
    evalReq.addEventListener("readystatechange", function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                //This is the evaluation from the backend
                let evaluation = JSON.parse(this.responseText);
                console.log(JSON.stringify(evaluation));
                //Import the current guideline in the modeler
                viewerColored.importXML(currentGuideline, function (err) {
                    if (err) {
                        //Something went wrong
                        console.log('Import failed');
                        return;
                    }
                    //Optimize view
                    let canvas = viewerColored.get('canvas');
                    canvas.zoom('fit-viewport');
                    //Color tasks according to timephase
                    setColor(evaluation);
                    updateSimplifiedGuidelineContainer(evaluation);
                });
            }
            //Request to backend failed
            else {
                alert('Evaluation failed. Check the format of the patient data.');
            }
        }
    });
    //Fill and send the request to the backend
    let data = createEvalReq(payload);
    //For docker: http://192.168.99.100:8080
    evalReq.open('POST', BACKEND_URL + '/rest/evaluatePatient');
    evalReq.setRequestHeader('Content-Type', 'application/json');
    evalReq.send(data);
});
