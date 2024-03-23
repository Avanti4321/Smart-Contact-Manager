package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    //method for adding common data to response
    @ModelAttribute
    public void addCommonData(Model model,Principal principal){
        String userName=principal.getName();
        System.out.println("USERNAME"+userName);
        User userByUserName = userRepository.getUserByUserName(userName);
        model.addAttribute("user",userByUserName);
        System.out.println("User:"+userByUserName);
    }
    //dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)
	{
		//get the user using username
        model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
    //open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title","Add Contact");
        model.addAttribute("contact",new Contact());
        return "normal/add_contact_form";
    }
    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session){
        try{

            String name=principal.getName();
            User user = this.userRepository.getUserByUserName(name);
            //processing and uploading file....
            if(file.isEmpty()){
                //if the file is empty then try our message
                System.out.println("File is Empty");
                contact.setImage("contact.png");
            }
            else{
                //upload the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("Added to data base");
            System.out.println("Data:"+contact);

            //success message
            session.setAttribute("message",new Message("Your contact is added!!Add more.......","success"));

        }catch(Exception e){
            System.out.println("ERROR"+e.getMessage());
            e.printStackTrace();
            //error message
            session.setAttribute("message",new Message("Something went wrong!!Try again...","danger"));

        }
        return "normal/add_contact_form";
    }

    //show contacts handler
    //per page=5 contact
    //current page=0[page]
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal){
        model.addAttribute("title","Show User Contacts");
        String userName=principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        //currentPage-page
        //Contact per page-5
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts =this.contactRepository.findContactsByUser(user.getId(),pageable);
//        List<Contact> contacts = user.getContacts();
         model.addAttribute("contacts",contacts);
         model.addAttribute("currentPage",page);
         model.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show_contacts";
    }

    //showing particular contact details
    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId")Integer cId,Model model,Principal principal){
        System.out.println("CID"+cId);
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        String userName=principal.getName();
        User userByUserName = this.userRepository.getUserByUserName(userName);
        if(userByUserName.getId()==contact.getUser().getId()){
            model.addAttribute("contact",contact);
            model.addAttribute("title",contact.getName());
        }
        return "normal/contactDetails";
    }
    //delete contact handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId")Integer cId,Model model,HttpSession session){
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        this.contactRepository.deleteByIdCustom(cId);
        session.setAttribute("message",new Message("Contact deleted successfully........","success"));
        return "redirect:/user/show-contacts/0";
    }

    //open update form handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model model){
        model.addAttribute("title","Update Contact");
        Contact contact = this.contactRepository.findById(cId).get();
        model.addAttribute("contact",contact);
        return "normal/update_form";
    }

    //update contact handler
    @RequestMapping(value = "/process-update",method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,HttpSession session,Principal principal){
        try{
            //old contact details
            Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

            //image
            if(!file.isEmpty()){
//                delete old photo
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1=new File(deleteFile,oldContactDetails.getImage());
                file1.delete();
//                update new photo
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            }else{
                contact.setImage(oldContactDetails.getImage());
            }
            User user=this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message",new Message("Your contact is updated .......","success"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //your profile handler

    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","profile Page");
        return "normal/profile";
    }


    //open setting handler
    @GetMapping("/settings")
    public String openSettings(){
        return "normal/settings";
    }

 //change password handler
   @RequestMapping(value = "/change-password",method = RequestMethod.POST)
    public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session){
        String userName = principal.getName();
        User currentUser = userRepository.getUserByUserName(userName);
        if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword())){
            //change the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            session.setAttribute("message",new Message(" Your password is successfully changed","alert-success"));
        }
        else{
            //error..
            session.setAttribute("message",new Message(" Your current password is  wrong!!","alert-danger"));
            return "redirect:/user/settings";

        }
        return "redirect:/user/index";
    }
}
