package com.gustavo.jobagent.opportunities.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectInfoController {
    @GetMapping("/api/v1/project")
    public ProjectInfo project() {
        return new ProjectInfo("job-agent", "M0", "Base del proyecto; evaluación y agente pendientes");
    }

    public record ProjectInfo(String name, String milestone, String description) { }
}
