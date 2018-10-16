var problemFilesDir = "/reojs/data/problems";
var templateFilesTable = new Map([
    ["java", "/reojs/data/templates/java"]
]);
var codeEditor;
var judgePageContent;


function initialize() {
    registerListeners();
    initOptions();
    setLayout();
    createCodeEditor("java");
}

function registerListeners() {
    window.addEventListener("message", function(event) {
        var data = JSON.parse(event.data);
        switch (data.title) {
            case "set_height":
                setPageHeight();
                break;
            case "enable_submission":
                enableSubmission(true);
                break;
        }
    }, false);
}

function initOptions() {
    $.get("data/options.json").done(function(data) {
        $.each(data, function(id, options) {
            $.each(options, function(text, attributes) {
                $(`#${id}`).append($("<option>", {
                    text: text,
                    value: attributes.value,
                    selected: attributes.selected === true
                }))
            })
        });
    });
}

function setLayout() {
    let padding = $("#top").height()+30;
    $("#main").css("padding-top", padding+"px");
}

function createCodeEditor(language) {
    codeEditor = CodeMirror($("#code_editor")[0], {
        lineNumbers: true,
        theme: "idea",
        indent: 4,
        tabSize: 4
    });

    let setTemplate = function(data) {
        codeEditor.setValue(data);
    }

    switch(language) {
        case "java":
        codeEditor.setOption("mode", "text/x-java");
            $.get(templateFilesTable.get("java")).done(setTemplate);
            break;
        default:
            throw new Error(`No such language: ${language}`);
    }
}

function onWindowResized() {
    let width = Math.max(960, Math.min(960, $(window).width()-100));
    let height = $(window).height() - $("#pages").position().top - 50;
    $("#iframe_problem").css({"width": width, "height": height});
}

function upload() {
    let filename = $("#source_file").val();
    if (filename.length >= 13) { // user_id + ".zip" = 13 chars
        let userId = filename.substring(filename.length-13, filename.length-4);
        if (new RegExp(/[A-Z][0-9]{8}/).test(userId)) {
            if ($("#source_file")[0].files[0].size > 102400) {
                alert("Your file is too large");
                return;
            }
            $("#user_id").val(userId);
            startJudgement();
            return;
        }
    }
    if (filename.length > 0) {
        alert("Wrong file name.");
    } // else is the case that the user canceld the select-file dialog.
}

function startJudgement() {
    if (!$("#source_file").val() && !codeEditor.getValue()) {
        alert("No content provided");
        return;
    }

    enableSubmission(false);
    $("#page_judge").html(judgePageContent);
    judgement = new Judgement(); // declared in judge.js
    showPage("judge");

    let formData = new FormData();
    formData.append("problem_id", $("#problem_id").val());
    formData.append("user_id", $("#user_id").val());
    formData.append("language", $("#language").val());
    if ($("#source_file").val()) {
        formData.append("source_file", $("#source_file")[0].files[0]);
        $("#source_file").val(null);
    } else {
        formData.append("source_text", codeEditor.getValue());
    }

    $.ajax({
        method: "POST",
        url: "register",
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
            let data = JSON.parse(response);
            if (data.name === "ticket_id") {
                let ticketId = data.value;
                console.log(`ticket_id=${ticketId}`);
                judgement.start(ticketId);
            } else if (data.name === "error") {
                new Judgement().onError(data.code, data.message);
            }
        }
    });

    if ($("#nav_judge").hasClass("nav_page_disabled")) {
        $("#nav_judge > a").attr("href", "#").click(function(){showPage("judge");});
        $("#nav_judge").removeClass("nav_page_disabled");
    }
    $("#iframe_hidden").html("");
}

function enableSubmission(enabled) {
    let button = document.getElementById("btn_submit");
    if (enabled) {
        button.removeAttribute("disabled");
        button.className = "btn_submit btn_submit_enabled";
        $("#source_file").attr("disabled", false);
        $("#upload_label").css("display", "inline");
    } else {
        button.setAttribute("disabled", true);
        button.className = "btn_submit";
        $("#source_file").attr("disabled", true);
        $("#upload_label").css("display", "none");
    }
}

function showPage(name) {
    if (name === "problem") {
        loadProblemPage();
    }

    $('.nav_container').children().removeClass('nav_page_active');
    $(`#nav_${name}`).addClass('nav_page_active');

    $("#pages").children().hide();
    $(`#page_${name}`).fadeIn(300);
}

function loadProblemPage() {
    let problem = $("#problem_id").val();
    let url = `third-party/pdfjs/web/viewer.html?file=${problemFilesDir}/${problem}.pdf`;
    if ($("#iframe_problem").attr("src") !== url) {
        $("#iframe_problem").attr("src", url);
    }
}

function onProblemChanged() {
    if ($("#page_problem").is(":visible")) {
        loadProblemPage();
    }
}

$(function() {
    initialize();

    judgePageContent = $("#page_judge").html();
    $("#title").click(function(){location.reload();});

    $("#pages").css("visibility", "visible");
    showPage("source_code");

    if (typeof (EventSource) === "undefined") {
        alert("Your browser does not support server-sent events.");
        enableSubmission(false);
        return;
    }
});
