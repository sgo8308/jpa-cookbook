package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {

        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
        /**
         * #jpa-springboot 준영속 상태의 엔티티를 수정할 때는 변경 감지 방법을 쓰자.
         *
         *
         * 준영속 상태의 엔티티를 업데이트 할 때는 2가지 방법이 있다.

         * 1. 변경 감지

         * 아래와 같이 준영속 엔티티의 id를 이용해 DB로부터 엔티티를 다시 찾아온 후에 변경된 값을 일일이 세팅해서
         * JPA의 변경 감지를 이용하는 방법이다.

         * 2. 머지 방식 - entitiyManager.merge(item);

         * 머지 메소드에 준영속 엔티티를 넣으면 마찬가지로 id를 이용해 준영속 엔티티의 id를 이용해 DB로부터 엔티티를 다시 찾아온다.
         * 근데 이 때 새롭게 세팅되지 않은 값은 Null로 처리해서 집어 넣는 다는 점이 다르다.
         * 즉 HTTP 메소드의 put처럼 작동한다는 것이다.

         * * 보통 수정할 때는 부분 수정만하지 전체 수정을 하는 경우는 거의 없고
         * 실수에 의해 원치않게 Null이 입력될 수 있으므로 실무에서는 변경 감지를 사용하는 것이 좋다.
         */
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }
}





