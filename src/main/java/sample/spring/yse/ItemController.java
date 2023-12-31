package sample.spring.yse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ItemController {
	private static final boolean isDebug = true;
	@Autowired
	ItemService itemService;

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public ModelAndView create() {
		return new ModelAndView("item/create");
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ModelAndView createPost(@RequestParam Map<String, Object> map) {
		ModelAndView mav = new ModelAndView();

		System.out.println(map);
		String itemId = this.itemService.create(map);
		if (itemId == null) {
			mav.setViewName("redirect:/create");
		} else {
			mav.setViewName("redirect:/detail?itemId=" + itemId);
		}

		return mav;
	}

	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public ModelAndView detail(@RequestParam Map<String, Object> map) {
		Map<String, Object> detailMap = this.itemService.detail(map);
		// postercid, title, category, price, insert_date, upload1, upload2, upload3,
		// upload4, upload5

		Map<String, Object> name_postercid = this.itemService.postercid(detailMap);
		// name

		Map<String, Object> idx = new HashMap<String, Object>();
		// idx

		ModelAndView mav = new ModelAndView();

		for (int i = 0; i < 5; i++) {
			if (detailMap.get("upload" + (i + 1)) != null) {
				idx.put("idx", detailMap.get("upload" + (i + 1)).toString());

				Map<String, Object> idx_image = this.itemService.upload(idx);

				String blobToBase64 = Base64Utils.encodeToString((byte[]) idx_image.get("image"));
				mav.addObject("upload" + (i + 1), blobToBase64);
			}

		}
		mav.addObject("data", detailMap);
		String itemId = map.get("itemId").toString();
		mav.addObject("itemId", itemId);
		mav.addObject("name", name_postercid);
		mav.setViewName("/item/detail");
		return mav;
	}

	@RequestMapping(value = "/update", method = RequestMethod.GET)
	public ModelAndView update(@RequestParam Map<String, Object> map) {
		Map<String, Object> detailMap = this.itemService.detail(map);

		ModelAndView mav = new ModelAndView();
		mav.addObject("data", detailMap);
		mav.setViewName("/item/update");
		return mav;
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	public ModelAndView updatePost(@RequestParam Map<String, Object> map) {
		ModelAndView mav = new ModelAndView();

		boolean isUpdateSuccess = this.itemService.edit(map);
		if (isUpdateSuccess) {
			String itemId = map.get("itemId").toString();
			mav.setViewName("redirect:/detail?itemId=" + itemId);
		} else {
			mav = this.update(map);
		}

		return mav;
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ModelAndView deletePost(@RequestParam Map<String, Object> map) {
		ModelAndView mav = new ModelAndView();

		boolean isDeleteSuccess = this.itemService.remove(map);
		if (isDeleteSuccess) {
			mav.setViewName("redirect:/list");
		} else {
			String itemId = map.get("itemId").toString();
			mav.setViewName("redirect:/detail?itemId=" + itemId);
		}

		return mav;
	}

	@RequestMapping(value = "list")
	public ModelAndView list(@RequestParam Map<String, Object> map) {

		List<Map<String, Object>> list = this.itemService.list(map);

		ModelAndView mav = new ModelAndView();
		mav.addObject("data", list);

		if (map.containsKey("keyword")) {
			mav.addObject("keyword", map.get("keyword"));
		}

		mav.setViewName("/item/list");
		return mav;
	}

	@RequestMapping(value = "/join", method = RequestMethod.GET)
	public ModelAndView join() {
		return new ModelAndView("member/join");
	}

	@RequestMapping(value = "/join", method = RequestMethod.POST)
	public ModelAndView joinMember(@RequestParam Map<String, Object> map) {
		ModelAndView mav = new ModelAndView();

		String pwd = map.get("password").toString();

		Map<String, Object> check = this.itemService.check(map);

		if (check != null) {
			mav.setViewName("redirect:/join");

		} else {
			String i = LoginCrypto.makeSalt();
			String j = LoginCrypto.hexSha1(pwd + i);

			map.put("salt", i);
			map.put("password", j);

			String bookId = this.itemService.join(map);
			if (bookId == null) {
				mav.setViewName("redirect:/create");
			} else {
				mav.setViewName("redirect:/join");
			}

		}

		return mav;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login() {
		return new ModelAndView("member/login");
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView loginMember(@RequestParam Map<String, Object> map, HttpServletRequest req) throws Exception {
		ModelAndView mav = new ModelAndView();

		HttpSession session = req.getSession();

		String pwd = map.get("password").toString();

		Map<String, Object> salt = this.itemService.salt(map);

		if (salt == null) {
			mav.setViewName("redirect:/login");

		} else {
			String passsalt = LoginCrypto.hexSha1(pwd + salt.get("salt"));

			map.put("password", passsalt);

			Map<String, Object> login = this.itemService.login(map);

			if (login == null) {
				session.setAttribute("member", null);
				mav.setViewName("redirect:/login");

			} else {
				session.setAttribute("member", login);
				mav.setViewName("redirect:/list");

			}

		}

		return mav;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public ModelAndView logout(HttpSession session) {
		ModelAndView mav = new ModelAndView();

		session.invalidate();
		mav.setViewName("redirect:/list");

		return mav;
	}

}
