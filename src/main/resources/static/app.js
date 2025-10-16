var autoRefreshIntervalId = null;
const dateTimeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm')

let demoDataId = null;
let scheduleId = null;
let loadedSchedule = null;

$(document).ready(function () {
  replaceQuickstartTimefoldAutoHeaderFooter();

  $("#solveButton").click(function () {
    solve();
  });
  $("#stopSolvingButton").click(function () {
    stopSolving();
  });
  $("#analyzeButton").click(function () {
    analyze();
  });
  $("#navConstraints").click(function () {
    loadConstraints();
  });

  setupAjax();
  fetchDemoData();
});

function setupAjax() {
  $.ajaxSetup({
    cache: false,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
    }
  });

  // Extend jQuery to support $.put() and $.delete()
  jQuery.each(["put", "delete"], function (i, method) {
    jQuery[method] = function (url, data, callback, type) {
      if (jQuery.isFunction(data)) {
        type = type || callback;
        callback = data;
        data = undefined;
      }
      return jQuery.ajax({
        url: url,
        type: method,
        dataType: type,
        data: data,
        success: callback
      });
    };
  });
}

function loadConstraints() {
  $.get("/api/constraint", function (data) {
    const tableBody = $("#constraintTable tbody");
    tableBody.empty();

    data.forEach(constraint => {
      console.log("check", constraint)
      const row = $("<tr>");
      row.append($("<td>").text(constraint.constraintName));
      row.append($("<td>").text(constraint.description));
      row.append($("<td>").text(constraint.constraintType))
      const toggle = $("<input>", {
        type: "checkbox",
        checked: constraint.enableFlag,
        change: function () {
          $.ajax({
            url: `/api/constraint/${constraint.id}/toggle`,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify({ enabled: this.checked }),
            success: () => console.log("Constraint updated:", constraint.constraintName),
            error: (xhr) => alert("Failed to update constraint: " + xhr.responseText)
          });
        }
      });
      row.append($("<td class=\"text-center\">").append(toggle));
      tableBody.append(row);
    });
  }).fail(() => {
    alert("Failed to load constraint settings.");
  });
}

function fetchDemoData() {
  $.get("/api/timetable/prepare/problem", function (data) {
    loadedSchedule = data;
    console.log("Solver result (demo):", loadedSchedule);   // check JSON shape
    refreshSchedule();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    // disable this page as there is no data
    let $demo = $("#demo");
    $demo.empty();
    $demo.html("<h1><p align=\"center\">No test data available</p></h1>")
  });
}

function refreshSchedule() {
  let path = "/api/timetable/" + scheduleId;
  if (scheduleId === null) {
    path = "/api/timetable/prepare/problem";
  }

  $.getJSON(path, function (schedule) {
    loadedSchedule = schedule;
    console.log("Solver result (json):", loadedSchedule);   // check JSON shape
    renderSchedule(schedule);
  })
    .fail(function (xhr, ajaxOptions, thrownError) {
      showError("Getting the timetable has failed.", xhr);
      refreshSolvingButtons(false);
    });
}

function renderSchedule(timetable) {
  refreshSolvingButtons(timetable.solverStatus != null && timetable.solverStatus !== "NOT_SOLVING");
  $("#score").text("Score: " + (timetable.score == null ? "?" : timetable.score));

  // Clear table sections
  const timetableByTeacher = $("#timetableByTeacher");
  timetableByTeacher.children().remove();
  const timetableByStudentGroup = $("#timetableByStudentGroup");
  timetableByStudentGroup.children().remove();
  const unassignedLessons = $("#unassignedLessons");
  unassignedLessons.children().remove();

  // ---------- Header: By Teacher ----------
  const theadByTeacher = $("<thead>").appendTo(timetableByTeacher);
  const headerRowByTeacher = $("<tr>").appendTo(theadByTeacher);
  headerRowByTeacher.append($("<th>Timeslot</th>"));

  const teachers = [
    ...new Set(
      timetable.lessons
        .map(lesson => {
          const teacher = lesson.teacher;
          if (teacher && (teacher.firstName || teacher.lastName)) {
            return `${teacher.firstName ?? ""} ${teacher.lastName ?? ""}`.trim();
          }
          return null;
        })
    )
  ].filter(Boolean);

  $.each(teachers, (index, teacher) => {
    headerRowByTeacher.append($("<th/>").append($("<span/>").text(teacher)));
  });

  // ---------- Header: By Student Group ----------
  const theadByStudentGroup = $("<thead>").appendTo(timetableByStudentGroup);
  const headerRowByStudentGroup = $("<tr>").appendTo(theadByStudentGroup);
  headerRowByStudentGroup.append($("<th>Timeslot</th>"));

  const studentGroups = [
    ...new Set(
      timetable.lessons
        .map(lesson => {
          const section = lesson.section;
          if (section && section.classMaster) {
            const className = section.classMaster.className || "";
            const sectionName = section.sectionName || "";
            if (className && sectionName) {
              return `${className}-${sectionName}`; // ✅ PreKG-A format
            } else if (className) {
              return className;
            } else {
              return sectionName;
            }
          }
          return null;
        })
    )
  ].filter(Boolean);

  $.each(studentGroups, (index, studentGroup) => {
    headerRowByStudentGroup
      .append($("<th/>").append($("<span/>").text(studentGroup)));
  });

  // ---------- Body: By Teacher / Student Group ----------
  const tbodyByTeacher = $("<tbody>").appendTo(timetableByTeacher);
  const tbodyByStudentGroup = $("<tbody>").appendTo(timetableByStudentGroup);

  const LocalTime = JSJoda.LocalTime;

  $.each(timetable.timeslots, (index, timeslot) => {
    const formattedTime = `${timeslot.dayOfWeek.charAt(0).toUpperCase() + timeslot.dayOfWeek.slice(1).toLowerCase()} ${LocalTime.parse(timeslot.startTime).format(dateTimeFormatter)} - ${LocalTime.parse(timeslot.endTime).format(dateTimeFormatter)}`;

    // Row by teacher
    const rowByTeacher = $("<tr>").appendTo(tbodyByTeacher);
    rowByTeacher.append($(`<th class="align-middle"/>`).append($("<span/>").text(formattedTime)));
    $.each(teachers, (index, teacher) => {
      rowByTeacher.append($("<td/>").prop("id", `timeslot${timeslot.id}teacher${convertToId(teacher)}`));
    });

    // Row by student group
    const rowByStudentGroup = $("<tr>").appendTo(tbodyByStudentGroup);
    rowByStudentGroup.append($(`<th class="align-middle"/>`).append($("<span/>").text(formattedTime)));
    $.each(studentGroups, (index, studentGroup) => {
      rowByStudentGroup.append($("<td/>").prop("id", `timeslot${timeslot.id}studentGroup${convertToId(studentGroup)}`));
    });
  });

  // ---------- Render Lessons ----------
  $.each(timetable.lessons, (index, lesson) => {
    if (!lesson.subject || !lesson.teacher || !lesson.section) return; // skip invalid lessons

    const color = pickColor(lesson.subject.subjectName);
    const subjectName = lesson.subject.subjectName || "";
    const teacherName = `${lesson.teacher.firstName ?? ""} ${lesson.teacher.lastName ?? ""}`.trim();

    const section = lesson.section;
    const studentGroupName = section && section.classMaster
      ? `${section.classMaster.className}-${section.sectionName}` // ✅ Match header key
      : (section?.classMaster?.className || section?.sectionName || "");

    const lessonElement = $(`<div class="card" style="background-color: ${color}"/>`)
      .append($(`<div class="card-body p-2"/>`)
        .append($(`<h5 class="card-title mb-1"/>`).text(subjectName))
        .append($(`<p class="card-text ms-2 mb-1"/>`).append($(`<em/>`).text(`by ${teacherName}`)))
        .append($(`<small class="ms-2 mt-1 card-text text-muted align-bottom float-end"/>`).text(lesson.id))
        .append($(`<p class="card-text ms-2"/>`).text(studentGroupName)));

    if (!lesson.timeslot) {
      unassignedLessons.append($(`<div class="col"/>`).append(lessonElement));
    } else {
      const timeslotId = lesson.timeslot.id;
      $(`#timeslot${timeslotId}teacher${convertToId(teacherName)}`).append(lessonElement.clone());
      $(`#timeslot${timeslotId}studentGroup${convertToId(studentGroupName)}`).append(lessonElement.clone());
    }
  });
}

function solve() {
  $.post("/api/timetable", JSON.stringify(loadedSchedule), function (data) {
    scheduleId = data;
    refreshSolvingButtons(true);
  }).fail(function (xhr, ajaxOptions, thrownError) {
      showError("Start solving failed.", xhr);
      refreshSolvingButtons(false);
    },
    "text");
}

function analyze() {
  new bootstrap.Modal("#scoreAnalysisModal").show()
  const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
  scoreAnalysisModalContent.children().remove();
  if (loadedSchedule.score == null) {
    scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
  } else {
    $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
    $.put("/api/timetable/analyze", JSON.stringify(loadedSchedule), function (scoreAnalysis) {
      let constraints = scoreAnalysis.constraints;
      constraints.sort((a, b) => {
        let aComponents = getScoreComponents(a.score), bComponents = getScoreComponents(b.score);
        if (aComponents.hard < 0 && bComponents.hard > 0) return -1;
        if (aComponents.hard > 0 && bComponents.soft < 0) return 1;
        if (Math.abs(aComponents.hard) > Math.abs(bComponents.hard)) {
          return -1;
        } else {
          if (aComponents.medium < 0 && bComponents.medium > 0) return -1;
          if (aComponents.medium > 0 && bComponents.medium < 0) return 1;
          if (Math.abs(aComponents.medium) > Math.abs(bComponents.medium)) {
            return -1;
          } else {
            if (aComponents.soft < 0 && bComponents.soft > 0) return -1;
            if (aComponents.soft > 0 && bComponents.soft < 0) return 1;

            return Math.abs(bComponents.soft) - Math.abs(aComponents.soft);
          }
        }
      });
      constraints.map((e) => {
        let components = getScoreComponents(e.weight);
        e.type = components.hard != 0 ? 'hard' : (components.medium != 0 ? 'medium' : 'soft');
        e.weight = components[e.type];
        let scores = getScoreComponents(e.score);
        e.implicitScore = scores.hard != 0 ? scores.hard : (scores.medium != 0 ? scores.medium : scores.soft);
      });
      scoreAnalysis.constraints = constraints;

      scoreAnalysisModalContent.children().remove();
      scoreAnalysisModalContent.text("");

      const analysisTable = $(`<table class="table"/>`).css({textAlign: 'center'});
      const analysisTHead = $(`<thead/>`).append($(`<tr/>`)
        .append($(`<th></th>`))
        .append($(`<th>Constraint</th>`).css({textAlign: 'left'}))
        .append($(`<th>Type</th>`))
        .append($(`<th># Matches</th>`))
        .append($(`<th>Weight</th>`))
        .append($(`<th>Score</th>`))
        .append($(`<th></th>`)));
      analysisTable.append(analysisTHead);
      const analysisTBody = $(`<tbody/>`)
      $.each(scoreAnalysis.constraints, (index, constraintAnalysis) => {
        let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
        if (!icon) icon = constraintAnalysis.weight < 0 && constraintAnalysis.matches.length == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

        let row = $(`<tr/>`);
        row.append($(`<td/>`).html(icon))
          .append($(`<td/>`).text(constraintAnalysis.name).css({textAlign: 'left'}))
          .append($(`<td/>`).text(constraintAnalysis.type))
          .append($(`<td/>`).html(`<b>${constraintAnalysis.matches.length}</b>`))
          .append($(`<td/>`).text(constraintAnalysis.weight))
          .append($(`<td/>`).text(constraintAnalysis.implicitScore));

        analysisTBody.append(row);

        if (constraintAnalysis.matches.length > 0) {
          let matchesRow = $(`<tr/>`).addClass("collapse").attr("id", "row" + index + "Collapse");
          let matchesListGroup = $(`<ul/>`).addClass('list-group').addClass('list-group-flush').css({textAlign: 'left'});

          $.each(constraintAnalysis.matches, (index2, match) => {
            matchesListGroup.append($(`<li/>`).addClass('list-group-item').addClass('list-group-item-light').text(match.justification.description));
          });

          matchesRow.append($(`<td/>`));
          matchesRow.append($(`<td/>`).attr('colspan', '6').append(matchesListGroup));
          analysisTBody.append(matchesRow);

          row.append($(`<td/>`).append($(`<a/>`).attr("data-toggle", "collapse").attr('href', "#row" + index + "Collapse").append($(`<span/>`).addClass('fas').addClass('fa-chevron-down')).click(e => {
            matchesRow.collapse('toggle');
            let target = $(e.target);
            if (target.hasClass('fa-chevron-down')) {
              target.removeClass('fa-chevron-down').addClass('fa-chevron-up');
            } else {
              target.removeClass('fa-chevron-up').addClass('fa-chevron-down');
            }
          })));
        } else {
          row.append($(`<td/>`));
        }

      });
      analysisTable.append(analysisTBody);
      scoreAnalysisModalContent.append(analysisTable);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Analyze failed.", xhr);
      },
      "text");
  }
}

function getScoreComponents(score) {
  let components = {hard: 0, medium: 0, soft: 0};

  $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], (i, parts) => {
    components[parts[2]] = parseInt(parts[1], 10);
  });

  return components;
}


function refreshSolvingButtons(solving) {
  if (solving) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
    }
  } else {
    $("#solveButton").show();
    $("#stopSolvingButton").hide();
    if (autoRefreshIntervalId != null) {
      clearInterval(autoRefreshIntervalId);
      autoRefreshIntervalId = null;
    }
  }
}

function stopSolving() {
  $.delete("/api/timetable/" + scheduleId, function () {
    refreshSolvingButtons(false);
    refreshSchedule();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}

function convertToId(str) {
  // Base64 encoding without padding to avoid XSS
  return btoa(str).replace(/=/g, "");
}

function copyTextToClipboard(id) {
  var text = $("#" + id).text().trim();

  var dummy = document.createElement("textarea");
  document.body.appendChild(dummy);
  dummy.value = text;
  dummy.select();
  document.execCommand("copy");
  document.body.removeChild(dummy);
}

// TODO: move to the webjar
function replaceQuickstartTimefoldAutoHeaderFooter() {
  const timefoldHeader = $("header#timefold-auto-header");
  if (timefoldHeader != null) {
    timefoldHeader.addClass("bg-black")
    timefoldHeader.append(
      $(`<div class="container-fluid">
        <nav class="navbar sticky-top navbar-expand-lg navbar-dark shadow mb-3">
          <a class="navbar-brand" href="https://timefold.ai">
            <img src="/webjars/timefold/img/timefold-logo-horizontal-negative.svg" alt="Timefold logo" width="200">
          </a>
          <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="nav nav-pills">
              <li class="nav-item active" id="navUIItem">
                <button class="nav-link active" id="navUI" data-bs-toggle="pill" data-bs-target="#demo" type="button">Demo UI</button>
              </li>
              <li class="nav-item" id="navRestItem">
                <button class="nav-link" id="navRest" data-bs-toggle="pill" data-bs-target="#rest" type="button">Guide</button>
              </li>
              <li class="nav-item" id="navOpenApiItem">
                <button class="nav-link" id="navOpenApi" data-bs-toggle="pill" data-bs-target="#openapi" type="button">REST API</button>
              </li>
            </ul>
          </div>
          <div class="ms-auto">
              <div class="dropdown">
                  <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                      Data
                  </button>
                  <div id="testDataButton" class="dropdown-menu" aria-labelledby="dropdownMenuButton"></div>
              </div>
          </div>
        </nav>
      </div>`));
  }

  const timefoldFooter = $("footer#timefold-auto-footer");
  if (timefoldFooter != null) {
    timefoldFooter.append(
      $(`<footer class="bg-black text-white-50">
               <div class="container">
                 <div class="hstack gap-3 p-4">
                   <div class="ms-auto"><a class="text-white" href="https://timefold.ai">Timefold</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://timefold.ai/docs">Documentation</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://github.com/TimefoldAI/timefold-quickstarts">Code</a></div>
                   <div class="vr"></div>
                   <div class="me-auto"><a class="text-white" href="https://timefold.ai/product/support/">Support</a></div>
                 </div>
               </div>
             </footer>`));
  }
}
