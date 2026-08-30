package dev.devsimulator.challenge.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record RunCodeRequest(@NotBlank String code) {}
