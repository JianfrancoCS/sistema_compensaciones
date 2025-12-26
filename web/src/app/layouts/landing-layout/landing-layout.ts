import { Component } from '@angular/core';
import { Navbar} from './components/navbar/navbar';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'app-landing-layout',
  imports: [Navbar, RouterOutlet],
  templateUrl: './landing-layout.html',
  standalone: true,
  styleUrl: './landing-layout.css'
})
export class LandingLayout {

}
