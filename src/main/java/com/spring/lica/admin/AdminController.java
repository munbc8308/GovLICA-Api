package com.spring.lica.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AdminController {

	private final SettingsService settingsService;

	@Autowired(required = false)
	private ContextRefresher contextRefresher;

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/admin/settings")
	public String settings(Model model) {
		try {
			model.addAttribute("sections", settingsService.loadSections());
		} catch (IOException e) {
			log.error("Failed to load settings", e);
			model.addAttribute("error", "설정 파일을 읽을 수 없습니다: " + e.getMessage());
		}
		return "settings";
	}

	@PostMapping("/admin/settings")
	public String saveSettings(@RequestParam Map<String, String> params, RedirectAttributes ra) {
		params.remove("_csrf");
		try {
			settingsService.saveProperties(params);
			if (contextRefresher != null) {
				contextRefresher.refresh();
				ra.addFlashAttribute("message", "설정이 저장되고 적용되었습니다.");
			} else {
				ra.addFlashAttribute("message", "설정이 저장되었습니다. 일부 변경사항은 재시작 후 적용됩니다.");
			}
		} catch (IOException e) {
			log.error("Failed to save settings", e);
			ra.addFlashAttribute("error", "설정 저장 실패: " + e.getMessage());
		}
		return "redirect:/admin/settings";
	}
}
