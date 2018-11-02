var testsetFilesDir = "data/problems";
var judgement;

var errorCodeTable = new Map([
    [0x1 << 0, "Invalid Source File"],
    [0x1 << 1, "Compile Error"],
    [0x1 << 2, "Execution Timeout"]
]);

var htmlSpecialChars = new Map([
    ["&", "&amp;"], ["<", "&lt;"], [">", "&gt;"], [`"`, "&quot;"], [`'`, "&#039;"]
]);


class Judgement {
    constructor() {
        this.setStatus("Pending");
        this.progressCircle = new ProgressBar.Circle("#progress", {
            color: "#71AAE3",
            strokeWidth: 3,
            trailWidth: 1,
            duration: 500,
            text: {
                value: "0",
                style : {
                    color: "#5193D5",
                    position: "absolute",
                    left: "50%",
                    top: "50%",
                    padding: 0,
                    margin: 0
                }
            },
            step: function(state, bar) {
                bar.setText((bar.value()*100).toFixed(0)+"%");
            }
        });
    }

    start(ticketId) {
        let obj = this;
        let eventSource = new EventSource(`judge?ticket_id=${ticketId}`);
        eventSource.onmessage = function(event) {
            let data = JSON.parse(event.data);
            if (data.name === "progress") {
                obj.showProgress(data.progress, data.status);
            } else if (data.name === "error") {
                eventSource.close();
                obj.onError(data.code, data.message);
            } else if (data.name === "report") {
                eventSource.close();
                obj.onResult(data);
            }
        }
    }

    setStatus(message, color="#5B8AE8") {
        $("#status").html(message);
        $("#status").css("color", color);
    }

    showProgress(progress, status) {
        let speed = (status === "Compiling") ? 1000 : 200;
        this.setStatus(status);
        this.progressCircle.animate(progress, {duration: speed});
    }

    onError(errorCode, message) {
        $("#progress").css("display", "none");
        this.setStatus(errorCodeTable.get(errorCode), "#D50E33");
        $("#judge_error").html(message).fadeIn(300);
        enableSubmission(true);
    }

    /**
     * The JSON data contains the following entries:
     * "problem_id": string, "num_passed": number, "num_testcases": number, "score": number,
     * "runtime": number, "results": JSON object {"passed": boolean, "timedout": boolean,  
     * "input": JSON array, "answer": string, "expected": string}.
     */
    onResult(report) {
        $("#progress").css("display", "none");
        this.setStatus("Finished", "#28B25F");
        this.printSummary(report);
        this.resultsTable = new JudgeResultsTable(report);
        this.resultsTable.print();

        $("#judge_summary").css("visibility", "visible").hide().fadeIn(300);
        $("#judge_results_container").css("visibility", "visible").hide()
                               .slideDown({duration: 500});
        enableSubmission(true);
    }

    printSummary(/* JSON */ result) {
        let testsetFile = `${result.problem_id}.json`;
        let numPassed = result.num_passed;
        let numTestCases = result.num_testcases;
        let passedPercent = (numPassed/numTestCases*100.0).toFixed(0);
        let score = result.score;
        let runtime = result.runtime;
        
        $("#testset_link > a").html(testsetFile)
                              .click(function(){showTestset(testsetFile)});
        $("#num_passed").html(`${numPassed}/${numTestCases}`);
        $("#passed_percent").html(`(${passedPercent}%)`);
        $("#score").html(score);
        $("#runtime").html(`(Runtime: ${runtime} ms)`);
    }
}


class JudgeResultsTable {
    constructor(/* JSON */ data) {
        this.data = data;
        this.numTestCases = data.num_testcases;
        this.collapsedRows = new Set(); // The set holds the row number being collapsed
        this.toggleOptions = {duration: 300};
    }

    prepareInputString(/* JSON array */ input) {
        let str = "";
        $.each(input, function(index, value) {
            str += escapeHtml(value) + "<hr>";
        });
        return str.substr(0, str.length-4); // Remove the last occurrence of <hr>
    }

    print() {
        let list = this.data.results;
        let numTestCases = this.data.num_testcases;

        for (let i=0; i<numTestCases; i++) {
            let result = list[i].passed ? "Accepted" : "Incorrect";
            let css_result = result.toLowerCase();

            let io_display = `<div id="io_display_${i}">CONTENT</div>`;
            let io_collpase = `<div id="io_collapse_${i}" class="io_collapse">...</div>`;

            let f = (text) => io_display.replace("CONTENT", text) + io_collpase;
            let input = f(this.prepareInputString(list[i].input));
            let answer = !list[i].timedout ? f(escapeHtml(list[i].answer)) :
                         `<span class="answer_timedout">Time limit exceeded</span>`;
            let expected = f(escapeHtml(list[i].expected));

            $("#judge_results tr:last").after(
                `<tr>` + 
                `<td class="id" onclick="toggleEachRow(${i})">${i+1}</td>` +
                `<td class="${css_result}">${result}</td>` + 
                `<td class="io">${input}</td>` + 
                `<td class="io">${answer}</td>` + 
                `<td class="io">${expected}</td>` +
                `</tr>`
            );            
        }
    }

    toggleEachRow(id) {
        $(`[id=io_display_${id}]`).slideToggle(this.toggleOptions);
        $(`[id=io_collapse_${id}]`).slideToggle(this.toggleOptions);

        if (this.collapsedRows.has(id)) {
            this.collapsedRows.delete(id);
        } else {
            this.collapsedRows.add(id);
        }
    }

    toggleWhole() {
        if (this.collapsedRows.size === 0) {
            // All rows are displayed. Collapse them.
            $(`[id^="io_display_"]`).each(function() {
                $(this).slideUp(this.toggleOptions);
            });
            let obj = this;
            let id = 0;
            $(`[id^="io_collapse_"]`).each(function() {
                $(this).slideDown(this.toggleOptions);
                obj.collapsedRows.add(id++);
            });
        } else {
            // Some rows are collapsed. Display them.
            $(`[id^="io_display_"]`).each(function() {
                $(this).slideDown(this.toggleOptions);
            });
            $(`[id^="io_collapse_"]`).each(function() {
                $(this).slideUp(this.toggleOptions);
            });
            this.collapsedRows.clear();
        }
    }
}


function escapeHtml(text) {
    return text.replace(/[&<>"']/g, function(c){return htmlSpecialChars.get(c);});
}

function showTestset(filename) {
    $.get(`${testsetFilesDir}/${filename}`).done(function(data) {
        let text = JSON.stringify(data, null, 2);
        $("#testset_text").val(text);
        $("#page_judge_mask").fadeIn(200);
        $("#testset_viewer").fadeIn(200);
    }).fail(function(jqXHR, status, error) {
        console.log(status);
        console.log(error);
    });
}

function copyTestset() {
    document.getElementById("testset_text").select();
    document.execCommand("copy");
}

function closeTestset() {
    $("#page_judge_mask").fadeOut(200);
    $("#testset_viewer").fadeOut(200);
}

function toggleEachRow(id) {
    judgement.resultsTable.toggleEachRow(id);
}

function toggleWhole() {
    judgement.resultsTable.toggleWhole();
}
